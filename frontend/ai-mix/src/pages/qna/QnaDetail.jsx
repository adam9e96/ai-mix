import { useEffect, useState } from "react";
import { Link, useParams, useNavigate } from "react-router-dom";
import "@styles/pages/qnadetail.css";

import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import rehypeRaw from "rehype-raw";
import { Prism as SyntaxHighlighter } from "react-syntax-highlighter";
import { oneDark } from "react-syntax-highlighter/dist/esm/styles/prism";
import {
  X,
  ChevronUp,
  ChevronDown,
  CheckCircle2,
  Copy,
  Check,
  BookOpen,
  MessageCircle,
  Network,
  Sparkles,
  Calendar,
} from "lucide-react";
import { FaUserCircle } from "react-icons/fa";
import { toast, ToastContainer } from "react-toastify";
import { AnimatePresence, motion } from "framer-motion";

import { useQnaDetailStore } from "@/stores/qna.detail.store";
import { useQnaStore } from "@/stores/qna.list.store";
import { useAuthStore } from "@/stores/auth.store";
import { useUIStore } from "@/stores/ui.store";
import BattleModal from "@/components/battle/BattleModal";
import LoginModal from "@/components/modal/LoginModal";
import LoadingDots from "@/components/common/LoadingDots";
import ToggleSwitch from "@/components/common/ToggleSwitch";
import axiosInstance from "@/api/axiosInstance";
import { EyeIcon, EyeOffIcon } from "@/components/common/Icons";
import QnaGraph from "@/components/qna/QnaGraph";

export default function QnaDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuthStore();
  const { battleOpen, setBattleOpen, showLoginModal, setShowLoginModal } =
    useUIStore();
  const { addSelectedTag, loadQnaList } = useQnaStore();

  const {
    question,
    answers,
    newAnswer,
    loading,
    loadingGPT,
    editMode,
    editForm,

    loadDetail,
    setEditMode,
    setEditForm,
    updateQuestion,
    deleteQuestion,

    setNewAnswer,
    submitAnswer,
    createGptAnswer,
    upvoteAnswer,
    downvoteAnswer,
    acceptAnswer,
    reset,
  } = useQnaDetailStore();

  const [anonPassword, setAnonPassword] = useState("");
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deletePassword, setDeletePassword] = useState("");
  const [votingAnswerId, setVotingAnswerId] = useState(null);
  const [showAcceptModal, setShowAcceptModal] = useState(false);
  const [acceptPassword, setAcceptPassword] = useState("");
  const [acceptingAnswerId, setAcceptingAnswerId] = useState(null);
  const [expandedAnswers, setExpandedAnswers] = useState(new Set());
  const [copiedCodeId, setCopiedCodeId] = useState(null);
  const [showEditPassword, setShowEditPassword] = useState(false);
  const [showDeletePassword, setShowDeletePassword] = useState(false);
  const [showAcceptPassword, setShowAcceptPassword] = useState(false);
  const [showTagGraphModal, setShowTagGraphModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [editPassword, setEditPassword] = useState("");
  const [showEditPasswordModal, setShowEditPasswordModal] = useState(false);

  // 태그 생성 관련 상태
  const [showTagModal, setShowTagModal] = useState(false);
  const [generatedTags, setGeneratedTags] = useState([]);
  const [selectedTags, setSelectedTags] = useState([]);
  const [isGeneratingTags, setIsGeneratingTags] = useState(false);
  const [tagPassword, setTagPassword] = useState("");
  const [showTagPassword, setShowTagPassword] = useState(false);

  // 지식 카드 생성 관련 상태
  const [isCreatingCard, setIsCreatingCard] = useState(false);
  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const [showKnowledgeCardModal, setShowKnowledgeCardModal] = useState(false);
  const [cardData, setCardData] = useState(null);
  const [editedCardData, setEditedCardData] = useState(null);

  // 사용자 정보 모달 관련 상태
  const [showUserModal, setShowUserModal] = useState(false);
  const [userInfo, setUserInfo] = useState(null);
  const [loadingUserInfo, setLoadingUserInfo] = useState(false);

  useEffect(() => {
    if (!id) return;
    loadDetail(id);
    // 화면 전환 시 스크롤을 상단으로 이동
    window.scrollTo(0, 0);
    return () => reset();
  }, [id, loadDetail, reset]);

  if (loading || !question) {
    return (
      <div className="qna-detail-wrapper">
        <div className="qna-detail-layout">
          <div className="qna-container">로딩중...</div>
          <div className="qna-fixed-sidebar">
            <button className="qna-sidebar-btn related-graph" disabled>
              <Network size={20} />
              <span>관련 게시물</span>
            </button>
          </div>
        </div>
      </div>
    );
  }

  // 작성자 확인: 비익명 게시물이고 로그인한 사용자가 작성자인 경우만 true
  const isOwner = (() => {
    if (!question || !user || question.isAnonymous) {
      return false;
    }

    const userId = user.id;
    const userEmail = user.email;
    const userNickname = user.nickname;

    const questionAuthorId = question.authorId;
    const questionAuthorEmail = question.authorEmail;
    const questionAuthorNickname = question.authorNickname;

    // ID 비교 (타입 변환 포함)
    const isOwnerById =
      userId != null &&
      questionAuthorId != null &&
      String(userId) === String(questionAuthorId);

    // 이메일 비교 (대소문자 무시)
    const isOwnerByEmail =
      userEmail &&
      questionAuthorEmail &&
      userEmail.toLowerCase().trim() ===
        questionAuthorEmail.toLowerCase().trim();

    // 닉네임 비교 (대소문자 무시, 공백 제거)
    const isOwnerByNickname =
      userNickname &&
      questionAuthorNickname &&
      userNickname.toLowerCase().trim() ===
        questionAuthorNickname.toLowerCase().trim();

    // ID, 이메일, 닉네임 중 하나라도 일치하면 작성자
    const result = isOwnerById || isOwnerByEmail || isOwnerByNickname;

    // 디버깅 정보 출력 (개발 환경에서만)
    if (import.meta.env.DEV) {
      console.log("작성자 확인:", {
        userId,
        userEmail,
        userNickname,
        questionAuthorId,
        questionAuthorEmail,
        questionAuthorNickname,
        isOwnerById,
        isOwnerByEmail,
        isOwnerByNickname,
        result,
      });
    }

    return result;
  })();

  // 수정/삭제 버튼 표시 조건:
  // - 익명 게시물: 항상 표시 (비밀번호로 인증)
  // - 비익명 게시물: 작성자만 표시
  const canEditDelete = question.isAnonymous || isOwner;

  // GPT 답변 존재 여부 체크
  const hasGptAnswer = answers.some((a) => a.answerType === "AI");

  // 지식 카드 생성에 사용할 답변 ID 찾기 (우선순위: 채택된 답변 > GPT 답변)
  const getAnswerIdForKnowledgeCard = () => {
    // 1. 채택된 답변 찾기
    const acceptedAnswer = answers.find((a) => a.isAccepted);
    if (acceptedAnswer) {
      return acceptedAnswer.id;
    }
    // 2. GPT 답변 찾기
    const gptAnswer = answers.find((a) => a.answerType === "AI");
    if (gptAnswer) {
      return gptAnswer.id;
    }
    return null;
  };

  // 답변 접기/펼치기 토글
  const toggleAnswerExpansion = (answerId) => {
    setExpandedAnswers((prev) => {
      const newSet = new Set(prev);
      if (newSet.has(answerId)) {
        newSet.delete(answerId);
      } else {
        newSet.add(answerId);
      }
      return newSet;
    });
  };

  // 사용자 정보 가져오기
  const fetchUserInfo = async (nickname) => {
    if (!nickname || nickname === "익명" || nickname === "AI-MIX") {
      return;
    }

    setLoadingUserInfo(true);
    setShowUserModal(true);
    try {
      const response = await axiosInstance.get(`/qna/users/${nickname}`);
      setUserInfo(response.data);
    } catch (error) {
      console.error("사용자 정보 불러오기 실패:", error);
      toast.error("사용자 정보를 불러올 수 없습니다.", {
        position: "top-right",
        autoClose: 2000,
      });
      setShowUserModal(false);
    } finally {
      setLoadingUserInfo(false);
    }
  };

  // 답변이 긴지 확인 (500자 이상) - GPT 답변 포함 모든 답변에 적용
  const isLongAnswer = (body) => {
    if (!body) return false;
    const bodyStr = String(body);
    // HTML 태그 제거 후 순수 텍스트 길이 체크
    const textContent = bodyStr.replace(/<[^>]*>/g, "");
    return textContent.length > 500;
  };

  // 답변 내용을 잘라서 보여주기
  // 코드 블록 렌더링 컴포넌트
  const getCodeBlockComponents = (answerId) => ({
    code({ node, className, children, ...props }) {
      const match = /language-(\w+)/.exec(className || "");
      const language = match ? match[1] : "";
      const codeString = String(children).replace(/\n$/, "");

      // 코드블록인 경우
      if (language) {
        const codeId = `code-${answerId}-${codeString
          .slice(0, 20)
          .replace(/\s/g, "")}`;

        const handleCopy = async () => {
          if (!codeString) return;
          try {
            await navigator.clipboard.writeText(codeString);
            setCopiedCodeId(codeId);
            setTimeout(() => setCopiedCodeId(null), 2000);
          } catch (err) {
            console.error("복사 실패:", err);
            const textArea = document.createElement("textarea");
            textArea.value = codeString;
            document.body.appendChild(textArea);
            textArea.select();
            document.execCommand("copy");
            document.body.removeChild(textArea);
            setCopiedCodeId(codeId);
            setTimeout(() => setCopiedCodeId(null), 2000);
          }
        };

        return (
          <div className="qna-code-block-wrapper">
            <SyntaxHighlighter
              language={language}
              style={oneDark}
              PreTag="div"
              customStyle={{
                margin: 0,
                padding: "18px 20px",
                borderRadius: "10px",
                fontSize: "14px",
                lineHeight: "1.6",
              }}
            >
              {codeString}
            </SyntaxHighlighter>
            {codeString && (
              <button
                className="qna-code-copy-btn"
                onClick={handleCopy}
                title="코드 복사"
              >
                {copiedCodeId === codeId ? (
                  <Check size={16} />
                ) : (
                  <Copy size={16} />
                )}
              </button>
            )}
          </div>
        );
      }

      // 인라인 코드인 경우
      return (
        <code className={className} {...props}>
          {children}
        </code>
      );
    },
    pre({ node, children, ...props }) {
      return <>{children}</>;
    },
  });

  const getTruncatedAnswer = (body) => {
    if (!body) return "";
    const bodyStr = String(body);
    // HTML 태그 제거 후 순수 텍스트 길이 체크
    const textContent = bodyStr.replace(/<[^>]*>/g, "");

    if (textContent.length <= 500) {
      return bodyStr;
    }

    // 원본에서 500자 정도의 텍스트에 해당하는 부분 찾기
    let charCount = 0;
    let truncatedIndex = bodyStr.length;

    for (let i = 0; i < bodyStr.length; i++) {
      // HTML 태그가 아닌 실제 텍스트 문자만 카운트
      if (bodyStr[i] === "<") {
        const tagEnd = bodyStr.indexOf(">", i);
        if (tagEnd !== -1) {
          i = tagEnd;
          continue;
        }
      }
      charCount++;
      if (charCount >= 500) {
        truncatedIndex = i + 1;
        break;
      }
    }

    return bodyStr.substring(0, truncatedIndex) + "...";
  };

  const handleAcceptAnswer = async () => {
    if (!acceptingAnswerId) return;

    try {
      await acceptAnswer(id, acceptingAnswerId, acceptPassword);
      // 성공 메시지는 store에서 처리하지 않으므로 여기서 처리
      // (토글 방식이므로 실제 채택/해제 여부는 서버 응답 후 데이터 재로드로 확인)
      toast.success("처리되었습니다");
      setShowAcceptModal(false);
      setAcceptPassword("");
      setAcceptingAnswerId(null);
    } catch (error) {
      // 에러는 store에서 이미 toast로 표시됨
      // 여기서는 모달 처리만 수행
      const errorMsg =
        error.response?.data?.message ||
        error.response?.data?.error ||
        error.message ||
        "답변 채택/해제에 실패했습니다";

      // 비밀번호 오류나 권한 오류 시 모달은 유지하고 비밀번호 입력란은 유지
      // (사용자가 다시 시도할 수 있도록)
      if (
        !errorMsg.includes("비밀번호") &&
        !errorMsg.includes("권한") &&
        !errorMsg.includes("작성자") &&
        !errorMsg.includes("일치") &&
        !errorMsg.includes("password")
      ) {
        setShowAcceptModal(false);
        setAcceptPassword("");
        setAcceptingAnswerId(null);
      } else {
        // 비밀번호 오류 시 입력란은 유지 (사용자가 다시 입력할 수 있도록)
        // 입력란을 비우지 않음
      }
    }
  };

  const handleDeleteQuestion = async () => {
    try {
      await deleteQuestion(
        id,
        navigate,
        question.isAnonymous ? deletePassword : null
      );
      toast.success("질문이 삭제되었습니다");
      setShowDeleteModal(false);
      setDeletePassword("");
      // navigate는 store에서 처리됨
    } catch (error) {
      const errorMsg =
        error.response?.data?.message ||
        error.message ||
        "질문 삭제에 실패했습니다";
      toast.error(errorMsg);
      // 비밀번호 오류 시 모달은 유지
      if (!errorMsg.includes("비밀번호")) {
        setShowDeleteModal(false);
        setDeletePassword("");
      }
    }
  };

  const handleEditPasswordConfirm = () => {
    if (!editPassword.trim()) {
      toast.error("비밀번호를 입력해주세요.");
      return;
    }
    // 비밀번호를 저장하고 수정 모드로 진입
    setAnonPassword(editPassword);
    setShowEditModal(false);
    setEditPassword("");
    setEditMode(true);
  };

  /* ================================
     지식카드 생성 모달 열기 및 미리보기 생성
  ================================= */
  const handleOpenKnowledgeCardModal = async () => {
    if (!id) {
      toast.error("질문 ID가 없습니다.");
      return;
    }

    if (!isAuthenticated()) {
      setShowLoginModal(true);
      return;
    }

    const answerId = getAnswerIdForKnowledgeCard();
    if (!answerId) {
      toast.error("지식 카드를 생성할 답변이 없습니다.", {
        containerId: "qna-knowledge-modal-toast",
      });
      return;
    }

    setIsAnalyzing(true);
    setShowKnowledgeCardModal(true);
    setCardData(null);

    try {
      // 1단계: 미리보기 생성
      const preview = await axiosInstance.post(
        "/knowledge/cards/from-qna/preview",
        {
          questionId: id,
          answerId: answerId,
        }
      );

      // 2단계: 미리보기 모달 표시 및 편집 가능한 데이터 초기화
      setCardData(preview.data);
      setEditedCardData({
        title: preview.data.title || "",
        oneLineDefinition: preview.data.oneLineDefinition || "",
        corePoints: preview.data.corePoints || [],
        commonMistakes: preview.data.commonMistakes || [],
        isPublished: true, // 기본값: 공개
      });
    } catch (error) {
      console.error("QnA 분석 실패:", error);
      const errorMessage =
        error.response?.data?.message ||
        error.response?.data?.error ||
        "QnA 분석 중 오류가 발생했습니다.";
      toast.error(errorMessage, { containerId: "qna-knowledge-modal-toast" });
      setShowKnowledgeCardModal(false);
    } finally {
      setIsAnalyzing(false);
    }
  };

  /* ================================
     지식카드 저장 확인
  ================================= */
  const handleConfirmSaveKnowledgeCard = async () => {
    if (!editedCardData) {
      toast.error("분석된 데이터가 없습니다.", {
        containerId: "qna-knowledge-modal-toast",
      });
      return;
    }

    if (!id) {
      toast.error("질문 ID가 없습니다.", {
        containerId: "qna-knowledge-modal-toast",
      });
      return;
    }

    const answerId = getAnswerIdForKnowledgeCard();
    if (!answerId) {
      toast.error("지식 카드를 생성할 답변이 없습니다.", {
        containerId: "qna-knowledge-modal-toast",
      });
      return;
    }

    // 필수 필드 검증
    if (!editedCardData.title?.trim()) {
      toast.error("제목을 입력해주세요.", {
        containerId: "qna-knowledge-modal-toast",
      });
      return;
    }

    if (!editedCardData.oneLineDefinition?.trim()) {
      toast.error("한 줄 정의를 입력해주세요.", {
        containerId: "qna-knowledge-modal-toast",
      });
      return;
    }

    setIsCreatingCard(true);
    try {
      // 3단계: 사용자 확인 후 저장
      const response = await axiosInstance.post("/knowledge/cards/from-qna", {
        title: editedCardData.title.trim(),
        oneLineDefinition: editedCardData.oneLineDefinition.trim(),
        corePoints: editedCardData.corePoints || [],
        commonMistakes: editedCardData.commonMistakes || [],
        sourceType: "QNA",
        sourceId: id,
        isPublished: editedCardData.isPublished !== false, // 기본값 true, false일 때만 비공개
        ...(question?.sourceId && {
          sourceId: question.sourceId,
        }),
      });

      // 토스트 알림 표시 (지식카드 목록으로 이동 버튼 포함)
      toast.success(
        ({ closeToast }) => (
          <div
            style={{ display: "flex", flexDirection: "column", gap: "10px" }}
          >
            <div
              style={{ fontSize: "14px", fontWeight: "500", color: "#1f2937" }}
            >
              지식 카드가 생성되었습니다.
            </div>
            <button
              onClick={() => {
                navigate("/knowledge");
                closeToast();
              }}
              style={{
                padding: "8px 16px",
                background: "#5a52e5",
                color: "#fff",
                border: "none",
                borderRadius: "6px",
                cursor: "pointer",
                fontSize: "13px",
                fontWeight: "600",
                alignSelf: "flex-start",
                transition: "background 0.2s",
                boxShadow: "0 2px 8px rgba(90, 82, 229, 0.3)",
              }}
              onMouseEnter={(e) => {
                if (e.target instanceof HTMLButtonElement) {
                  e.target.style.background = "#4a42d5";
                }
              }}
              onMouseLeave={(e) => {
                if (e.target instanceof HTMLButtonElement) {
                  e.target.style.background = "#5a52e5";
                }
              }}
            >
              지식카드 목록으로 이동
            </button>
          </div>
        ),
        {
          position: "top-right",
          autoClose: 5000,
          style: { zIndex: 1000000 },
        }
      );

      setShowKnowledgeCardModal(false);
      setCardData(null);
      setEditedCardData(null);
    } catch (error) {
      console.error("지식카드 저장 실패:", error);
      const errorMessage =
        error.response?.data?.message ||
        error.response?.data?.error ||
        "지식카드 저장 중 오류가 발생했습니다.";
      toast.error(errorMessage, { containerId: "qna-knowledge-modal-toast" });
    } finally {
      setIsCreatingCard(false);
    }
  };

  /* ================================
     카드 데이터 수정 핸들러
  ================================= */
  const handleCardDataChange = (field, value) => {
    setEditedCardData((prev) => ({
      ...prev,
      [field]: value,
    }));
  };

  const handleAddArrayItem = (field) => {
    setEditedCardData((prev) => ({
      ...prev,
      [field]: [...(prev[field] || []), ""],
    }));
  };

  const handleUpdateArrayItem = (field, index, value) => {
    setEditedCardData((prev) => {
      const newArray = [...(prev[field] || [])];
      newArray[index] = value;
      return {
        ...prev,
        [field]: newArray,
      };
    });
  };

  const handleRemoveArrayItem = (field, index) => {
    setEditedCardData((prev) => {
      const newArray = [...(prev[field] || [])];
      newArray.splice(index, 1);
      return {
        ...prev,
        [field]: newArray,
      };
    });
  };

  /* ================================
     태그 생성 관련 함수
  ================================= */
  // 태그 프리뷰 생성
  const generateTags = async () => {
    if (!id) {
      toast.error("질문 ID가 없습니다.");
      return;
    }

    // 익명 질문인 경우 비밀번호 검증
    if (question?.isAnonymous) {
      if (!tagPassword.trim()) {
        toast.error("익명 질문의 태그를 생성하려면 비밀번호를 입력해주세요.");
        return;
      }
    }

    setIsGeneratingTags(true);
    try {
      const requestBody = {
        questionId: id,
      };

      // 익명 질문인 경우 비밀번호 포함
      if (question?.isAnonymous) {
        requestBody.anonymousPassword = tagPassword.trim();
      }

      console.log("태그 생성 요청:", {
        questionId: id,
        isAnonymous: question?.isAnonymous,
        hasPassword: !!requestBody.anonymousPassword,
      });

      const res = await axiosInstance.post("/qna/tags/preview", requestBody);

      if (res.data && res.data.tags && Array.isArray(res.data.tags)) {
        setGeneratedTags(res.data.tags);
        // 프리뷰만 보여주기 위해 선택된 태그 초기화
        setSelectedTags([]);
        toast.success("태그 프리뷰가 생성되었습니다.");
      } else {
        toast.error("태그 생성에 실패했습니다.");
      }
    } catch (e) {
      console.error("태그 생성 오류", e);
      const errorMessage =
        e.response?.data?.message ||
        e.response?.data?.error ||
        "태그 생성에 실패했습니다.";

      // 비밀번호 관련 에러인 경우 더 명확한 메시지 표시
      if (
        errorMessage.includes("비밀번호") ||
        errorMessage.includes("password") ||
        e.response?.status === 401 ||
        e.response?.status === 403
      ) {
        toast.error(
          "비밀번호가 일치하지 않습니다. 올바른 비밀번호를 입력해주세요."
        );
      } else {
        toast.error(errorMessage);
      }
    } finally {
      setIsGeneratingTags(false);
    }
  };

  // 태그 선택/해제
  const toggleTag = (tag) => {
    setSelectedTags((prev) =>
      prev.includes(tag) ? prev.filter((t) => t !== tag) : [...prev, tag]
    );
  };

  // 태그 적용
  const applyTags = async () => {
    if (!id) {
      toast.error("질문 ID가 없습니다.");
      return;
    }

    if (selectedTags.length === 0) {
      toast.error("태그를 선택해주세요.");
      return;
    }

    // 익명 질문인 경우 비밀번호 검증
    if (question?.isAnonymous) {
      if (!tagPassword.trim()) {
        toast.error("비밀번호를 입력해주세요.");
        return;
      }
    }

    try {
      // 태그 업데이트를 위한 페이로드 구성
      const updatePayload = {
        tags: selectedTags,
      };

      // 익명 질문인 경우 비밀번호 포함
      if (question?.isAnonymous) {
        updatePayload.anonymousPassword = tagPassword.trim();
      }

      console.log("태그 적용 요청:", {
        questionId: id,
        isAnonymous: question?.isAnonymous,
        hasPassword: !!updatePayload.anonymousPassword,
        tagsCount: selectedTags.length,
      });

      await axiosInstance.patch(`/qna/questions/${id}/tags`, updatePayload);
      toast.success("태그가 적용되었습니다.");
      setShowTagModal(false);
      setTagPassword("");
      setGeneratedTags([]);
      setSelectedTags([]);
      // 질문 정보 다시 불러오기
      loadDetail(id);
    } catch (e) {
      console.error("태그 업데이트 오류", e);
      const errorMessage =
        e.response?.data?.message ||
        e.response?.data?.error ||
        "태그 적용에 실패했습니다.";

      // 비밀번호 관련 에러인 경우 더 명확한 메시지 표시
      if (
        errorMessage.includes("비밀번호") ||
        errorMessage.includes("password") ||
        e.response?.status === 401 ||
        e.response?.status === 403
      ) {
        toast.error(
          "비밀번호가 일치하지 않습니다. 올바른 비밀번호를 입력해주세요."
        );
      } else {
        toast.error(errorMessage);
      }
    }
  };

  return (
    <div className="qna-detail-wrapper">
      <div className="qna-detail-layout">
        <div className="qna-container">
          <Link to="/qna" className="qna-back-btn">
            ← 목록으로
          </Link>

          {/* 질문 영역 */}
          {!editMode ? (
            <>
              <div className="qna-title-row">
                <h2 className="qna-detail-title">{question.title}</h2>

                <div className="qna-action-buttons">
                  {/* 태그 생성 버튼 */}
                  <button
                    className="qna-tag-generate-btn"
                    onClick={() => setShowTagModal(true)}
                  >
                    <Sparkles size={14} />
                    <span>태그 생성</span>
                  </button>

                  {canEditDelete && (
                    <>
                      <button
                        onClick={() => {
                          if (question.isAnonymous) {
                            setShowEditModal(true);
                          } else {
                            setEditMode(true);
                          }
                        }}
                      >
                        수정
                      </button>
                      <button
                        className="danger"
                        onClick={() => setShowDeleteModal(true)}
                      >
                        삭제
                      </button>
                    </>
                  )}
                </div>
              </div>

              <div className="qna-detail-info">
                <span
                  className="qna-detail-author clickable"
                  onClick={() => {
                    if (!question.isAnonymous && question.authorNickname) {
                      fetchUserInfo(question.authorNickname);
                    }
                  }}
                  style={{
                    cursor: question.isAnonymous ? "default" : "pointer",
                  }}
                >
                  {question.isAnonymous ? (
                    <FaUserCircle className="qna-detail-author-icon" />
                  ) : question.authorAvatarUrl ? (
                    <div
                      style={{
                        width: "20px",
                        height: "20px",
                        borderRadius: "50%",
                        padding: "2px",
                        background:
                          "linear-gradient(135deg, #7d5cf6 0%, #6a4ee6 100%)",
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                      }}
                    >
                      <img
                        src={question.authorAvatarUrl}
                        alt={question.authorNickname}
                        style={{
                          width: "100%",
                          height: "100%",
                          borderRadius: "50%",
                          objectFit: "cover",
                        }}
                      />
                    </div>
                  ) : (
                    <FaUserCircle className="qna-detail-author-icon" />
                  )}
                  <span className="qna-detail-author-name">
                    {question.isAnonymous ? "익명" : question.authorNickname}
                  </span>
                </span>
                <span className="qna-detail-date">
                  {question.createdAt?.slice(0, 10)}
                </span>
              </div>

              {/* 태그 */}
              {question.tags && question.tags.length > 0 && (
                <div
                  className="qna-detail-tags"
                  style={{
                    display: "flex",
                    alignItems: "center",
                    gap: "12px",
                    flexWrap: "wrap",
                    marginTop: "8px",
                    marginBottom: "8px",
                  }}
                >
                  {question.tags.map((tag, index) => (
                    <span
                      key={index}
                      className="qna-detail-tag clickable"
                      onClick={(e) => {
                        e.preventDefault();
                        e.stopPropagation();
                        addSelectedTag(tag);
                        navigate(`/qna?tag=${encodeURIComponent(tag)}`);
                        loadQnaList(0, "title", "");
                      }}
                      style={{ cursor: "pointer" }}
                    >
                      {tag}
                    </span>
                  ))}
                </div>
              )}

              <div className="qna-detail-body">
                <ReactMarkdown
                  remarkPlugins={[remarkGfm]}
                  rehypePlugins={[rehypeRaw]}
                  components={getCodeBlockComponents(question.id)}
                >
                  {question.body}
                </ReactMarkdown>
              </div>
            </>
          ) : (
            <>
              <input
                className="qna-edit-title"
                value={editForm.title}
                onChange={(e) => setEditForm("title", e.target.value)}
              />

              <textarea
                className="qna-edit-body"
                value={editForm.body}
                onChange={(e) => setEditForm("body", e.target.value)}
              />

              {question.isAnonymous && (
                <div className="pw-box">
                  <input
                    type={showEditPassword ? "text" : "password"}
                    className="anon-password-input pw-input"
                    placeholder="익명 질문 비밀번호"
                    value={anonPassword}
                    onChange={(e) => setAnonPassword(e.target.value)}
                  />
                  <div
                    className="pw-eye"
                    onClick={() => setShowEditPassword(!showEditPassword)}
                  >
                    {showEditPassword ? (
                      <EyeIcon size={20} color="#666" />
                    ) : (
                      <EyeOffIcon size={20} color="#666" />
                    )}
                  </div>
                </div>
              )}

              <div className="qna-edit-actions">
                <button onClick={() => updateQuestion(id, anonPassword)}>
                  저장
                </button>
                <button onClick={() => setEditMode(false)}>취소</button>
              </div>
            </>
          )}

          <hr />

          {/* 답변 목록 */}
          <div className="qna-answer-section">
            <h3>답변 목록</h3>

            {answers.length === 0 && (
              <div className="qna-empty-answer">
                <MessageCircle size={48} className="qna-empty-answer-icon" />
                <p className="qna-empty-answer-text">
                  아직 등록된 답변이 없습니다.
                </p>
                <p className="qna-empty-answer-subtext">
                  첫 번째 답변을 작성해보세요!
                </p>
              </div>
            )}

            {answers.map((a) => (
              <div
                key={a.id}
                className={`qna-answer-card ${
                  a.answerType === "AI" ? "ai-answer-card" : "user-answer-card"
                } ${a.isAccepted ? "adopted" : ""}`}
              >
                <div className="qna-answer-content">
                  {/* Upvote 버튼 영역 */}
                  <div className="qna-answer-vote">
                    <button
                      className={`qna-vote-btn qna-vote-up ${
                        a.userVote === "UP" ? "active" : ""
                      }`}
                      onClick={async () => {
                        if (!isAuthenticated()) {
                          setShowLoginModal(true);
                          return;
                        }
                        if (votingAnswerId === a.id) return; // 이미 처리 중이면 무시
                        setVotingAnswerId(a.id);
                        try {
                          await upvoteAnswer(id, a.id);
                        } finally {
                          setVotingAnswerId(null);
                        }
                      }}
                      disabled={votingAnswerId === a.id}
                      title={a.userVote === "UP" ? "추천 취소" : "추천"}
                    >
                      <ChevronUp size={20} />
                    </button>
                    <span
                      className={`qna-vote-count ${
                        (a.score || 0) > 0
                          ? "positive"
                          : (a.score || 0) < 0
                          ? "negative"
                          : ""
                      }`}
                    >
                      {(a.score || 0) > 0 ? "+" : ""}
                      {a.score || 0}
                    </span>
                    <button
                      className={`qna-vote-btn qna-vote-down ${
                        a.userVote === "DOWN" ? "active" : ""
                      }`}
                      onClick={async () => {
                        if (!isAuthenticated()) {
                          setShowLoginModal(true);
                          return;
                        }
                        if (votingAnswerId === a.id) return; // 이미 처리 중이면 무시
                        setVotingAnswerId(a.id);
                        try {
                          await downvoteAnswer(id, a.id);
                        } finally {
                          setVotingAnswerId(null);
                        }
                      }}
                      disabled={votingAnswerId === a.id}
                      title={a.userVote === "DOWN" ? "비추천 취소" : "비추천"}
                    >
                      <ChevronDown size={20} />
                    </button>
                  </div>

                  {/* 답변 내용 영역 */}
                  <div className="qna-answer-main">
                    <div className="qna-answer-header">
                      <div className="qna-answer-left">
                        <span
                          className="qna-answer-author clickable"
                          onClick={() => {
                            if (a.answerType !== "AI" && a.authorNickname) {
                              fetchUserInfo(a.authorNickname);
                            }
                          }}
                          style={{
                            cursor:
                              a.answerType === "AI" ? "default" : "pointer",
                          }}
                        >
                          {a.answerType === "AI" ? null : a.authorAvatarUrl ? (
                            <div
                              style={{
                                width: "20px",
                                height: "20px",
                                borderRadius: "50%",
                                padding: "2px",
                                background:
                                  "linear-gradient(135deg, #7d5cf6 0%, #6a4ee6 100%)",
                                display: "flex",
                                alignItems: "center",
                                justifyContent: "center",
                              }}
                            >
                              <img
                                src={a.authorAvatarUrl}
                                alt={a.authorNickname}
                                style={{
                                  width: "100%",
                                  height: "100%",
                                  borderRadius: "50%",
                                  objectFit: "cover",
                                }}
                              />
                            </div>
                          ) : (
                            <FaUserCircle className="qna-answer-author-icon" />
                          )}
                          <span className="qna-answer-author-name">
                            {a.answerType === "AI"
                              ? "AI-MIX"
                              : a.authorNickname}
                          </span>
                        </span>
                        <span
                          className={`qna-answer-badge ${
                            a.answerType === "AI" ? "ai" : "user"
                          }`}
                        >
                          {a.answerType === "AI" ? "🤖 AI 답변" : "🙋 사용자"}
                        </span>
                        {a.isAccepted && (
                          <span className="qna-adopted-badge">
                            <CheckCircle2 size={16} />
                            채택됨
                          </span>
                        )}
                      </div>
                      <div className="qna-answer-right">
                        {/* 채택 버튼: 이미 채택된 답변이 아닌 경우에만 표시 */}
                        {!a.isAccepted && (
                          <button
                            className="qna-accept-btn"
                            onClick={() => {
                              if (question.isAnonymous) {
                                // 익명 질문인 경우 비밀번호 모달 표시
                                setAcceptingAnswerId(a.id);
                                setShowAcceptModal(true);
                              } else {
                                // 비익명 질문인 경우
                                if (!isAuthenticated()) {
                                  setShowLoginModal(true);
                                  return;
                                }
                                // 비익명 질문은 비밀번호 없이 호출 (빈 객체 전송)
                                acceptAnswer(id, a.id, null).catch((error) => {
                                  // 에러는 store에서 처리됨
                                });
                              }
                            }}
                            title="이 답변을 채택합니다"
                          >
                            <CheckCircle2 size={18} />
                            채택하기
                          </button>
                        )}
                        <span className="qna-answer-date">
                          {a.createdAt?.slice(0, 10)}
                        </span>
                      </div>
                    </div>

                    <div className="qna-answer-body-wrapper">
                      <div
                        className={`qna-answer-body ${
                          isLongAnswer(a.body) && !expandedAnswers.has(a.id)
                            ? "qna-answer-collapsed"
                            : ""
                        }`}
                      >
                        {(() => {
                          const isLong = isLongAnswer(a.body);
                          const isExpanded = expandedAnswers.has(a.id);

                          if (isLong && !isExpanded) {
                            // 긴 답변이고 접혀있는 경우
                            return (
                              <div className="qna-answer-content-wrapper">
                                <ReactMarkdown
                                  remarkPlugins={[remarkGfm]}
                                  rehypePlugins={[rehypeRaw]}
                                  components={getCodeBlockComponents(a.id)}
                                >
                                  {getTruncatedAnswer(a.body)}
                                </ReactMarkdown>
                              </div>
                            );
                          } else {
                            // 짧은 답변이거나 펼쳐진 경우
                            return (
                              <ReactMarkdown
                                remarkPlugins={[remarkGfm]}
                                rehypePlugins={[rehypeRaw]}
                                components={getCodeBlockComponents(a.id)}
                              >
                                {a.body || ""}
                              </ReactMarkdown>
                            );
                          }
                        })()}
                      </div>
                      {/* 더보기/접기 버튼은 답변 본문 밖에 배치하여 항상 보이도록 */}
                      {(() => {
                        const isLong = isLongAnswer(a.body);
                        const isExpanded = expandedAnswers.has(a.id);

                        if (isLong && !isExpanded) {
                          return (
                            <button
                              className="qna-expand-btn"
                              onClick={() => toggleAnswerExpansion(a.id)}
                            >
                              더보기
                            </button>
                          );
                        } else if (isLong && isExpanded) {
                          return (
                            <button
                              className="qna-expand-btn"
                              onClick={() => toggleAnswerExpansion(a.id)}
                            >
                              접기
                            </button>
                          );
                        }
                        return null;
                      })()}
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>

          <hr />

          {/* 답변 작성 - 로그인한 경우에만 표시 */}
          {isAuthenticated() ? (
            <div className="qna-new-answer-box">
              <textarea
                placeholder="답변 내용을 입력하세요..."
                value={newAnswer}
                onChange={(e) => setNewAnswer(e.target.value)}
              />
              <button
                className="qna-submit-btn"
                disabled={!newAnswer.trim()}
                onClick={() => submitAnswer(id)}
              >
                답변 등록
              </button>
            </div>
          ) : (
            <div className="qna-new-answer-box qna-login-required">
              <div className="qna-login-message">
                <MessageCircle size={24} />
                <p>답변을 작성하려면 로그인이 필요합니다.</p>
                <button
                  className="qna-login-btn"
                  onClick={() => setShowLoginModal(true)}
                >
                  로그인하기
                </button>
              </div>
            </div>
          )}
        </div>

        {/* 우측 고정 사이드바 - 관련 게시물 버튼만 (게시물 바로 옆) */}
        <div className="qna-fixed-sidebar">
          <button
            className={`qna-sidebar-btn related-graph ${
              showTagGraphModal ? "active" : ""
            }`}
            onClick={() => {
              if (!isAuthenticated()) {
                setShowLoginModal(true);
                return;
              }
              setShowTagGraphModal(!showTagGraphModal);
            }}
            title="관련 게시물 그래프"
          >
            <Network size={20} />
            <span>관련 게시물</span>
          </button>
        </div>
      </div>

      {/* 하단 액션 바 */}
      <div className="qna-action-bar">
        <button
          className="qna-action-btn gpt"
          onClick={() => {
            if (!isAuthenticated()) {
              setShowLoginModal(true);
              return;
            }
            createGptAnswer(id);
          }}
          disabled={loadingGPT}
        >
          {loadingGPT ? "GPT 생성 중..." : "🤖 GPT에게 답변 요청"}
        </button>

        {hasGptAnswer && isAuthenticated() && (
          <button
            className="qna-action-btn knowledge"
            onClick={handleOpenKnowledgeCardModal}
            disabled={isCreatingCard || isAnalyzing}
            style={{
              background: "linear-gradient(135deg, #6c63ff 0%, #5a52e5 100%)",
            }}
          >
            <BookOpen size={18} style={{ marginRight: "6px" }} />
            지식카드 생성
          </button>
        )}

        <button
          className="qna-action-btn battle"
          onClick={() => {
            if (!isAuthenticated()) {
              setShowLoginModal(true);
              return;
            }
            setBattleOpen(true);
          }}
        >
          ⚔️ 배틀하기
        </button>
      </div>

      {/* 로그인 모달 */}
      {showLoginModal && (
        <LoginModal
          onClose={() => setShowLoginModal(false)}
          onSuccess={() => {
            // 로그인 성공 후 필요한 작업 수행
          }}
        />
      )}

      {/* 배틀 모달 */}
      {battleOpen && (
        <BattleModal
          id={id}
          onClose={() => setBattleOpen(false)}
          sourceType="QNA"
        />
      )}

      {/* 삭제 확인 모달 (익명 질문) */}
      {showDeleteModal && question?.isAnonymous && (
        <div className="qna-delete-anonymous-modal-backdrop">
          <div className="qna-delete-anonymous-modal-box">
            <button
              className="qna-delete-anonymous-modal-close"
              onClick={() => {
                setShowDeleteModal(false);
                setDeletePassword("");
              }}
            >
              <X size={20} />
            </button>

            <h3>질문 삭제</h3>

            <div style={{ marginBottom: "20px" }}>
              <p style={{ marginBottom: "12px", color: "#666" }}>
                익명 질문을 삭제하려면 작성 시 사용한 비밀번호를 입력해주세요.
              </p>
              <div className="pw-box">
                <input
                  type={showDeletePassword ? "text" : "password"}
                  className="qna-delete-anonymous-modal-input pw-input"
                  placeholder="비밀번호를 입력하세요"
                  value={deletePassword}
                  onChange={(e) => setDeletePassword(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === "Enter" && deletePassword.trim()) {
                      handleDeleteQuestion();
                    }
                  }}
                  autoFocus
                />
                <div
                  className="pw-eye"
                  onClick={() => setShowDeletePassword(!showDeletePassword)}
                >
                  {showDeletePassword ? (
                    <EyeIcon size={20} color="#666" />
                  ) : (
                    <EyeOffIcon size={20} color="#666" />
                  )}
                </div>
              </div>
            </div>

            <div className="qna-delete-anonymous-modal-btn-group">
              <button
                className="qna-delete-anonymous-modal-btn qna-delete-anonymous-modal-btn-cancel"
                onClick={() => {
                  setShowDeleteModal(false);
                  setDeletePassword("");
                }}
              >
                취소
              </button>
              <button
                className="qna-delete-anonymous-modal-btn qna-delete-anonymous-modal-btn-delete"
                onClick={handleDeleteQuestion}
                disabled={!deletePassword.trim()}
              >
                삭제
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 삭제 확인 모달 (비익명 질문) */}
      {showDeleteModal && !question?.isAnonymous && (
        <div className="qna-delete-modal-backdrop">
          <div className="qna-delete-modal-box">
            <button
              className="qna-delete-modal-close"
              onClick={() => setShowDeleteModal(false)}
            >
              <X size={20} />
            </button>

            <h3>질문 삭제</h3>

            <div style={{ marginBottom: "20px" }}>
              <p style={{ marginBottom: "12px", color: "#666" }}>
                정말로 이 질문을 삭제하시겠습니까?
              </p>
              <p
                style={{
                  marginTop: "12px",
                  fontSize: "0.85rem",
                  color: "#999",
                }}
              >
                삭제된 질문은 복구할 수 없습니다.
              </p>
            </div>

            <div className="qna-delete-modal-btn-group">
              <button
                className="qna-delete-modal-btn qna-delete-modal-btn-cancel"
                onClick={() => setShowDeleteModal(false)}
              >
                취소
              </button>
              <button
                className="qna-delete-modal-btn qna-delete-modal-btn-delete"
                onClick={handleDeleteQuestion}
              >
                삭제
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 수정 비밀번호 확인 모달 (익명 질문) */}
      {showEditModal && question?.isAnonymous && (
        <div className="qna-delete-anonymous-modal-backdrop">
          <div className="qna-delete-anonymous-modal-box">
            <button
              className="qna-delete-anonymous-modal-close"
              onClick={() => {
                setShowEditModal(false);
                setEditPassword("");
              }}
            >
              <X size={20} />
            </button>

            <h3>질문 수정</h3>

            <div style={{ marginBottom: "20px" }}>
              <p style={{ marginBottom: "12px", color: "#666" }}>
                익명 질문을 수정하려면 작성 시 사용한 비밀번호를 입력해주세요.
              </p>
              <div className="pw-box">
                <input
                  type={showEditPasswordModal ? "text" : "password"}
                  className="qna-delete-anonymous-modal-input pw-input"
                  placeholder="비밀번호를 입력하세요"
                  value={editPassword}
                  onChange={(e) => setEditPassword(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === "Enter" && editPassword.trim()) {
                      handleEditPasswordConfirm();
                    }
                  }}
                  autoFocus
                />
                <div
                  className="pw-eye"
                  onClick={() =>
                    setShowEditPasswordModal(!showEditPasswordModal)
                  }
                >
                  {showEditPasswordModal ? (
                    <EyeIcon size={20} color="#666" />
                  ) : (
                    <EyeOffIcon size={20} color="#666" />
                  )}
                </div>
              </div>
            </div>

            <div className="qna-delete-anonymous-modal-btn-group">
              <button
                className="qna-delete-anonymous-modal-btn qna-delete-anonymous-modal-btn-cancel"
                onClick={() => {
                  setShowEditModal(false);
                  setEditPassword("");
                }}
              >
                취소
              </button>
              <button
                className="qna-delete-anonymous-modal-btn qna-delete-anonymous-modal-btn-delete"
                onClick={handleEditPasswordConfirm}
                disabled={!editPassword.trim()}
              >
                확인
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 태그 생성 모달 */}
      <AnimatePresence>
        {showTagModal && (
          <motion.div
            className="tag-modal-backdrop"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={() => {
              setShowTagModal(false);
              setTagPassword("");
              setGeneratedTags([]);
              setSelectedTags([]);
            }}
          >
            <motion.div
              className="tag-modal-container"
              initial={{ scale: 0.9, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.9, opacity: 0 }}
              onClick={(e) => e.stopPropagation()}
            >
              <div className="tag-modal-header">
                <h3>태그 생성</h3>
                <button
                  className="tag-modal-close"
                  onClick={() => {
                    setShowTagModal(false);
                    setTagPassword("");
                    setGeneratedTags([]);
                    setSelectedTags([]);
                  }}
                >
                  <X size={20} />
                </button>
              </div>

              <div className="tag-modal-content">
                {/* 익명 질문인 경우 비밀번호 입력 */}
                {question?.isAnonymous && (
                  <div className="tag-password-section">
                    <label className="tag-password-label">
                      익명 질문 비밀번호
                    </label>
                    <div className="pw-box">
                      <input
                        type={showTagPassword ? "text" : "password"}
                        className="anon-password-input pw-input"
                        placeholder="질문 작성 시 사용한 비밀번호를 입력하세요"
                        value={tagPassword}
                        onChange={(e) => setTagPassword(e.target.value)}
                      />
                      <div
                        className="pw-eye"
                        onClick={() => setShowTagPassword(!showTagPassword)}
                      >
                        {showTagPassword ? (
                          <EyeIcon size={20} color="#666" />
                        ) : (
                          <EyeOffIcon size={20} color="#666" />
                        )}
                      </div>
                    </div>
                  </div>
                )}

                {/* 태그 생성 버튼 */}
                <div className="tag-modal-actions">
                  <button
                    className="tag-generate-btn"
                    onClick={generateTags}
                    disabled={isGeneratingTags}
                  >
                    <Sparkles size={16} />
                    {isGeneratingTags ? "태그 생성 중..." : "태그 자동 생성"}
                  </button>
                </div>

                {/* 생성된 태그 프리뷰 및 선택 */}
                {generatedTags.length > 0 && (
                  <div className="tag-section">
                    <div className="tag-section-label">
                      생성된 태그 프리뷰 (원하는 태그를 선택해주세요)
                    </div>
                    <div className="tag-list">
                      {generatedTags.map((tag, index) => (
                        <button
                          key={index}
                          className={`tag-item ${
                            selectedTags.includes(tag) ? "selected" : ""
                          }`}
                          onClick={() => toggleTag(tag)}
                        >
                          {tag}
                        </button>
                      ))}
                    </div>
                    {selectedTags.length > 0 && (
                      <div className="tag-actions">
                        <button className="tag-update-btn" onClick={applyTags}>
                          태그 적용하기
                        </button>
                      </div>
                    )}
                  </div>
                )}
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* 관련 게시물 그래프 모달 */}
      <AnimatePresence>
        {showTagGraphModal && (
          <motion.div
            className="qna-related-graph-modal-backdrop"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={() => setShowTagGraphModal(false)}
          >
            <motion.div
              className="qna-related-graph-modal-box"
              initial={{ opacity: 0, scale: 0.9, y: 20 }}
              animate={{ opacity: 1, scale: 1, y: 0 }}
              exit={{ opacity: 0, scale: 0.9, y: 20 }}
              transition={{ type: "spring", damping: 25 }}
              onClick={(e) => e.stopPropagation()}
            >
              <div className="qna-related-graph-modal-header">
                <h3>관련 게시물 그래프</h3>
                <button
                  className="qna-related-graph-modal-close"
                  onClick={() => setShowTagGraphModal(false)}
                >
                  <X size={20} />
                </button>
              </div>
              <div className="qna-related-graph-modal-content">
                {id ? (
                  <div
                    style={{
                      width: "100%",
                      height: "100%",
                      position: "relative",
                    }}
                  >
                    <QnaGraph centerQuestionId={id} maxNodes={30} />
                  </div>
                ) : (
                  <div
                    style={{
                      padding: "40px",
                      textAlign: "center",
                      color: "#6b7280",
                      height: "100%",
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                    }}
                  >
                    질문 ID를 찾을 수 없습니다.
                  </div>
                )}
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* 지식카드 생성 모달 */}
      <AnimatePresence>
        {showKnowledgeCardModal && (
          <motion.div
            className="chat-knowledge-modal-backdrop"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={() => {
              setShowKnowledgeCardModal(false);
              setIsCreatingCard(false);
              setIsAnalyzing(false);
              setCardData(null);
              setEditedCardData(null);
            }}
          >
            <motion.div
              className="chat-knowledge-modal-box"
              initial={{ opacity: 0, scale: 0.9, y: 20 }}
              animate={{ opacity: 1, scale: 1, y: 0 }}
              exit={{ opacity: 0, scale: 0.9, y: 20 }}
              transition={{ type: "spring", damping: 25 }}
              onClick={(e) => e.stopPropagation()}
            >
              <ToastContainer
                containerId="qna-knowledge-modal-toast"
                position="top-center"
                autoClose={2000}
                hideProgressBar={false}
                pauseOnHover
                theme="colored"
                style={{ zIndex: 10000 }}
              />
              <button
                className="chat-knowledge-modal-close"
                onClick={() => {
                  setShowKnowledgeCardModal(false);
                  setIsCreatingCard(false);
                  setIsAnalyzing(false);
                  setCardData(null);
                  setEditedCardData(null);
                }}
              >
                <X size={20} />
              </button>

              <h3>지식카드 생성</h3>

              {isAnalyzing ? (
                <div className="analyzing-container">
                  <LoadingDots />
                  <p style={{ marginTop: "20px", color: "#666" }}>
                    QnA를 분석 중입니다...
                  </p>
                </div>
              ) : editedCardData ? (
                <>
                  <div className="knowledge-card-preview">
                    {/* 제목 */}
                    <div className="form-group">
                      <label>제목 *</label>
                      <input
                        type="text"
                        className="card-edit-input"
                        value={editedCardData.title}
                        onChange={(e) =>
                          handleCardDataChange("title", e.target.value)
                        }
                        placeholder="제목을 입력하세요"
                      />
                    </div>

                    {/* 한 줄 정의 */}
                    <div className="form-group">
                      <label>한 줄 정의 *</label>
                      <textarea
                        className="card-edit-textarea"
                        value={editedCardData.oneLineDefinition}
                        onChange={(e) =>
                          handleCardDataChange(
                            "oneLineDefinition",
                            e.target.value
                          )
                        }
                        placeholder="한 줄 정의를 입력하세요"
                        rows={3}
                      />
                    </div>

                    {/* 핵심 포인트 */}
                    <div className="form-group">
                      <label>핵심 포인트</label>
                      {editedCardData.corePoints &&
                      editedCardData.corePoints.length > 0 ? (
                        <div className="array-edit-container">
                          {editedCardData.corePoints.map((point, index) => (
                            <div key={index} className="array-edit-item">
                              <input
                                type="text"
                                className="card-edit-input"
                                value={point}
                                onChange={(e) =>
                                  handleUpdateArrayItem(
                                    "corePoints",
                                    index,
                                    e.target.value
                                  )
                                }
                                placeholder={`핵심 포인트 ${index + 1}`}
                              />
                              <button
                                type="button"
                                className="array-remove-btn"
                                onClick={() =>
                                  handleRemoveArrayItem("corePoints", index)
                                }
                                title="삭제"
                              >
                                <X size={16} />
                              </button>
                            </div>
                          ))}
                        </div>
                      ) : null}
                      <button
                        type="button"
                        className="array-add-btn"
                        onClick={() => handleAddArrayItem("corePoints")}
                      >
                        + 핵심 포인트 추가
                      </button>
                    </div>

                    {/* 흔한 실수 */}
                    <div className="form-group">
                      <label>흔한 실수</label>
                      {editedCardData.commonMistakes &&
                      editedCardData.commonMistakes.length > 0 ? (
                        <div className="array-edit-container">
                          {editedCardData.commonMistakes.map(
                            (mistake, index) => (
                              <div key={index} className="array-edit-item">
                                <input
                                  type="text"
                                  className="card-edit-input"
                                  value={mistake}
                                  onChange={(e) =>
                                    handleUpdateArrayItem(
                                      "commonMistakes",
                                      index,
                                      e.target.value
                                    )
                                  }
                                  placeholder={`흔한 실수 ${index + 1}`}
                                />
                                <button
                                  type="button"
                                  className="array-remove-btn"
                                  onClick={() =>
                                    handleRemoveArrayItem(
                                      "commonMistakes",
                                      index
                                    )
                                  }
                                  title="삭제"
                                >
                                  <X size={16} />
                                </button>
                              </div>
                            )
                          )}
                        </div>
                      ) : null}
                      <button
                        type="button"
                        className="array-add-btn"
                        onClick={() => handleAddArrayItem("commonMistakes")}
                      >
                        + 흔한 실수 추가
                      </button>
                    </div>

                    {/* 공개/비공개 설정 */}
                    <div className="form-group">
                      <div
                        style={{
                          display: "flex",
                          justifyContent: "space-between",
                          alignItems: "center",
                          padding: "12px 0",
                          borderTop: "1px solid #e5e7eb",
                          marginTop: "8px",
                        }}
                      >
                        <div>
                          <label
                            style={{
                              display: "block",
                              marginBottom: "4px",
                              fontWeight: 600,
                              color: "#374151",
                            }}
                          >
                            공개 설정
                          </label>
                          <span
                            style={{
                              fontSize: "0.875rem",
                              color: "#6b7280",
                            }}
                          >
                            {editedCardData.isPublished !== false
                              ? "모든 사용자가 볼 수 있습니다"
                              : "나만 볼 수 있습니다"}
                          </span>
                        </div>
                        <ToggleSwitch
                          checked={editedCardData.isPublished !== false}
                          onChange={(checked) => {
                            handleCardDataChange("isPublished", checked);
                          }}
                        />
                      </div>
                    </div>
                  </div>

                  <div
                    style={{ display: "flex", gap: "10px", marginTop: "20px" }}
                  >
                    <button
                      className="chat-knowledge-modal-btn"
                      onClick={handleConfirmSaveKnowledgeCard}
                      style={{
                        background: "#6c63ff",
                        flex: 1,
                      }}
                      disabled={isCreatingCard}
                    >
                      {isCreatingCard ? "저장 중..." : "저장하기"}
                    </button>
                    <button
                      className="chat-knowledge-modal-btn"
                      onClick={() => {
                        setShowKnowledgeCardModal(false);
                        setIsCreatingCard(false);
                        setCardData(null);
                        setEditedCardData(null);
                      }}
                      style={{
                        background: "#6b7280",
                        flex: 1,
                      }}
                      disabled={isCreatingCard}
                    >
                      취소
                    </button>
                  </div>
                </>
              ) : (
                <div className="analyzing-container">
                  <p style={{ color: "#666" }}>분석 결과를 불러오는 중...</p>
                </div>
              )}
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* 채택 확인 모달 (익명 질문) */}
      {showAcceptModal && question?.isAnonymous && (
        <div className="qna-accept-modal-backdrop">
          <div className="qna-accept-modal-box">
            <button
              className="qna-accept-modal-close"
              onClick={() => {
                setShowAcceptModal(false);
                setAcceptPassword("");
                setAcceptingAnswerId(null);
              }}
            >
              <X size={20} />
            </button>

            <h3>답변 채택</h3>

            <div style={{ marginBottom: "20px" }}>
              <p style={{ marginBottom: "12px", color: "#666" }}>
                익명 질문의 답변을 채택하려면 질문 작성 시 사용한 비밀번호를
                입력해주세요.
              </p>
              <p
                style={{
                  marginBottom: "12px",
                  fontSize: "0.85rem",
                  color: "#999",
                }}
              >
                질문 작성 시 입력한 비밀번호와 동일한 비밀번호를 입력해야
                합니다.
              </p>
              <div className="pw-box">
                <input
                  type={showAcceptPassword ? "text" : "password"}
                  className="qna-accept-modal-input pw-input"
                  placeholder="질문 작성 시 사용한 비밀번호를 입력하세요"
                  value={acceptPassword}
                  onChange={(e) => setAcceptPassword(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === "Enter" && acceptPassword.trim()) {
                      handleAcceptAnswer();
                    }
                  }}
                  autoFocus
                />
                <div
                  className="pw-eye"
                  onClick={() => setShowAcceptPassword(!showAcceptPassword)}
                >
                  {showAcceptPassword ? (
                    <EyeIcon size={20} color="#666" />
                  ) : (
                    <EyeOffIcon size={20} color="#666" />
                  )}
                </div>
              </div>
            </div>

            <div className="qna-accept-modal-btn-group">
              <button
                className="qna-accept-modal-btn qna-accept-modal-btn-cancel"
                onClick={() => {
                  setShowAcceptModal(false);
                  setAcceptPassword("");
                  setAcceptingAnswerId(null);
                }}
              >
                취소
              </button>
              <button
                className="qna-accept-modal-btn qna-accept-modal-btn-delete"
                onClick={handleAcceptAnswer}
                disabled={!acceptPassword.trim()}
                style={{
                  background:
                    "linear-gradient(135deg, #22c55e 0%, #16a34a 100%)",
                }}
              >
                채택하기
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 사용자 정보 모달 */}
      {showUserModal && (
        <div
          style={{
            position: "fixed",
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            background: "rgba(0, 0, 0, 0.5)",
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            zIndex: 10000,
          }}
          onClick={() => {
            setShowUserModal(false);
            setUserInfo(null);
          }}
        >
          <div
            style={{
              background: "#fff",
              borderRadius: "24px",
              padding: "32px",
              maxWidth: "500px",
              width: "90%",
              maxHeight: "80vh",
              overflow: "auto",
              boxShadow: "0 20px 60px rgba(125, 92, 246, 0.2)",
              border: "1px solid rgba(125, 92, 246, 0.1)",
            }}
            onClick={(e) => e.stopPropagation()}
          >
            {loadingUserInfo ? (
              <div style={{ textAlign: "center", padding: "40px" }}>
                <LoadingDots />
              </div>
            ) : userInfo ? (
              <>
                <div
                  style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    marginBottom: "24px",
                  }}
                >
                  <h3
                    style={{
                      fontSize: "24px",
                      fontWeight: "700",
                      margin: 0,
                      background:
                        "linear-gradient(135deg, #7d5cf6 0%, #6a4ee6 100%)",
                      WebkitBackgroundClip: "text",
                      WebkitTextFillColor: "transparent",
                      backgroundClip: "text",
                    }}
                  >
                    사용자 정보
                  </h3>
                  <button
                    onClick={() => {
                      setShowUserModal(false);
                      setUserInfo(null);
                    }}
                    style={{
                      background: "none",
                      border: "none",
                      fontSize: "24px",
                      cursor: "pointer",
                      color: "#666",
                      padding: "4px",
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                    }}
                  >
                    <X size={20} />
                  </button>
                </div>

                <div
                  style={{
                    display: "flex",
                    flexDirection: "column",
                    gap: "20px",
                  }}
                >
                  {/* 프로필 이미지 및 닉네임 */}
                  <div
                    style={{
                      display: "flex",
                      alignItems: "center",
                      gap: "16px",
                    }}
                  >
                    {userInfo.avatarUrl ? (
                      <div
                        style={{
                          width: "80px",
                          height: "80px",
                          borderRadius: "50%",
                          padding: "3px",
                          background:
                            "linear-gradient(135deg, #7d5cf6 0%, #6a4ee6 100%)",
                        }}
                      >
                        <img
                          src={userInfo.avatarUrl}
                          alt={userInfo.nickname}
                          style={{
                            width: "100%",
                            height: "100%",
                            borderRadius: "50%",
                            objectFit: "cover",
                          }}
                        />
                      </div>
                    ) : (
                      <div
                        style={{
                          width: "80px",
                          height: "80px",
                          borderRadius: "50%",
                          background:
                            "linear-gradient(135deg, #7d5cf6 0%, #6a4ee6 100%)",
                          display: "flex",
                          alignItems: "center",
                          justifyContent: "center",
                        }}
                      >
                        <FaUserCircle size={40} color="#fff" />
                      </div>
                    )}
                    <div>
                      <h4
                        style={{
                          fontSize: "20px",
                          fontWeight: "600",
                          margin: "0 0 4px 0",
                          color: "#7d5cf6",
                        }}
                      >
                        {userInfo.nickname}
                      </h4>
                      {userInfo.bio && (
                        <div
                          style={{
                            marginTop: "8px",
                            padding: "12px 16px",
                            background:
                              "linear-gradient(135deg, #f8f9ff 0%, #f3f4ff 100%)",
                            border: "1px solid rgba(125, 92, 246, 0.2)",
                            borderRadius: "12px",
                          }}
                        >
                          <p
                            style={{
                              fontSize: "14px",
                              color: "#555",
                              margin: 0,
                              lineHeight: "1.6",
                              fontWeight: "500",
                            }}
                          >
                            {userInfo.bio}
                          </p>
                        </div>
                      )}
                    </div>
                  </div>

                  {/* 통계 정보 */}
                  {userInfo.statistics && (
                    <div
                      style={{
                        background:
                          "linear-gradient(135deg, #f8f9ff 0%, #f3f4ff 100%)",
                        borderRadius: "16px",
                        padding: "24px",
                        border: "1px solid rgba(125, 92, 246, 0.2)",
                      }}
                    >
                      <h5
                        style={{
                          fontSize: "16px",
                          fontWeight: "600",
                          margin: "0 0 20px 0",
                          color: "#7d5cf6",
                          display: "flex",
                          alignItems: "center",
                          gap: "8px",
                        }}
                      >
                        <Sparkles size={18} color="#7d5cf6" />
                        활동 통계
                      </h5>
                      <div
                        style={{
                          display: "grid",
                          gridTemplateColumns: "repeat(2, 1fr)",
                          gap: "12px",
                        }}
                      >
                        <div
                          style={{
                            background: "#fff",
                            padding: "16px",
                            borderRadius: "12px",
                            border: "1px solid rgba(125, 92, 246, 0.2)",
                            boxShadow: "0 2px 8px rgba(125, 92, 246, 0.1)",
                            transition: "all 0.2s",
                          }}
                          onMouseEnter={(e) => {
                            e.currentTarget.style.transform =
                              "translateY(-2px)";
                            e.currentTarget.style.boxShadow =
                              "0 4px 12px rgba(125, 92, 246, 0.2)";
                          }}
                          onMouseLeave={(e) => {
                            e.currentTarget.style.transform = "translateY(0)";
                            e.currentTarget.style.boxShadow =
                              "0 2px 8px rgba(125, 92, 246, 0.1)";
                          }}
                        >
                          <div
                            style={{
                              fontSize: "12px",
                              color: "#666",
                              marginBottom: "6px",
                              fontWeight: "500",
                            }}
                          >
                            질문
                          </div>
                          <div
                            style={{
                              fontSize: "24px",
                              fontWeight: "700",
                              background:
                                "linear-gradient(135deg, #7d5cf6 0%, #6a4ee6 100%)",
                              WebkitBackgroundClip: "text",
                              WebkitTextFillColor: "transparent",
                              backgroundClip: "text",
                            }}
                          >
                            {userInfo.statistics.questionCount || 0}
                          </div>
                        </div>
                        <div
                          style={{
                            background: "#fff",
                            padding: "16px",
                            borderRadius: "12px",
                            border: "1px solid rgba(34, 197, 94, 0.2)",
                            boxShadow: "0 2px 8px rgba(34, 197, 94, 0.1)",
                            transition: "all 0.2s",
                          }}
                          onMouseEnter={(e) => {
                            e.currentTarget.style.transform =
                              "translateY(-2px)";
                            e.currentTarget.style.boxShadow =
                              "0 4px 12px rgba(34, 197, 94, 0.2)";
                          }}
                          onMouseLeave={(e) => {
                            e.currentTarget.style.transform = "translateY(0)";
                            e.currentTarget.style.boxShadow =
                              "0 2px 8px rgba(34, 197, 94, 0.1)";
                          }}
                        >
                          <div
                            style={{
                              fontSize: "12px",
                              color: "#666",
                              marginBottom: "6px",
                              fontWeight: "500",
                            }}
                          >
                            답변
                          </div>
                          <div
                            style={{
                              fontSize: "24px",
                              fontWeight: "700",
                              color: "#22c55e",
                            }}
                          >
                            {userInfo.statistics.answerCount || 0}
                          </div>
                        </div>
                        <div
                          style={{
                            background: "#fff",
                            padding: "16px",
                            borderRadius: "12px",
                            border: "1px solid rgba(245, 158, 11, 0.2)",
                            boxShadow: "0 2px 8px rgba(245, 158, 11, 0.1)",
                            transition: "all 0.2s",
                          }}
                          onMouseEnter={(e) => {
                            e.currentTarget.style.transform =
                              "translateY(-2px)";
                            e.currentTarget.style.boxShadow =
                              "0 4px 12px rgba(245, 158, 11, 0.2)";
                          }}
                          onMouseLeave={(e) => {
                            e.currentTarget.style.transform = "translateY(0)";
                            e.currentTarget.style.boxShadow =
                              "0 2px 8px rgba(245, 158, 11, 0.1)";
                          }}
                        >
                          <div
                            style={{
                              fontSize: "12px",
                              color: "#666",
                              marginBottom: "6px",
                              fontWeight: "500",
                            }}
                          >
                            채택된 답변
                          </div>
                          <div
                            style={{
                              fontSize: "24px",
                              fontWeight: "700",
                              color: "#f59e0b",
                            }}
                          >
                            {userInfo.statistics.acceptedAnswerCount || 0}
                          </div>
                        </div>
                        <div
                          style={{
                            background: "#fff",
                            padding: "16px",
                            borderRadius: "12px",
                            border: "1px solid rgba(59, 130, 246, 0.2)",
                            boxShadow: "0 2px 8px rgba(59, 130, 246, 0.1)",
                            transition: "all 0.2s",
                          }}
                          onMouseEnter={(e) => {
                            e.currentTarget.style.transform =
                              "translateY(-2px)";
                            e.currentTarget.style.boxShadow =
                              "0 4px 12px rgba(59, 130, 246, 0.2)";
                          }}
                          onMouseLeave={(e) => {
                            e.currentTarget.style.transform = "translateY(0)";
                            e.currentTarget.style.boxShadow =
                              "0 2px 8px rgba(59, 130, 246, 0.1)";
                          }}
                        >
                          <div
                            style={{
                              fontSize: "12px",
                              color: "#666",
                              marginBottom: "6px",
                              fontWeight: "500",
                            }}
                          >
                            총 점수
                          </div>
                          <div
                            style={{
                              fontSize: "24px",
                              fontWeight: "700",
                              color: "#3b82f6",
                            }}
                          >
                            {userInfo.statistics.totalScore || 0}
                          </div>
                        </div>
                      </div>
                    </div>
                  )}

                  {/* 가입일 */}
                  {userInfo.createdAt && (
                    <div
                      style={{
                        fontSize: "14px",
                        color: "#666",
                        paddingTop: "16px",
                        borderTop: "1px solid rgba(125, 92, 246, 0.2)",
                        display: "flex",
                        alignItems: "center",
                        gap: "8px",
                      }}
                    >
                      <Calendar size={16} color="#7d5cf6" />
                      <span>
                        가입일:{" "}
                        {new Date(userInfo.createdAt).toLocaleDateString(
                          "ko-KR",
                          {
                            year: "numeric",
                            month: "long",
                            day: "numeric",
                          }
                        )}
                      </span>
                    </div>
                  )}
                </div>
              </>
            ) : null}
          </div>
        </div>
      )}
    </div>
  );
}
