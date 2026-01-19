import { create } from "zustand";
import instance from "@/api/axiosInstance";
import { toast } from "react-toastify";

export const useQnaDetailStore = create((set, get) => ({
  /* ==============================
     상태
  ============================== */
  question: null,
  answers: [],
  newAnswer: "",

  loading: false,
  loadingGPT: false,

  editMode: false,
  editForm: {
    title: "",
    body: "",
    isAnonymous: false,
  },

  /* ==============================
     상세 조회 (UUID)
  ============================== */
  loadDetail: async (questionId) => {
    if (!questionId) return;

    try {
      set({ loading: true });

      const res = await instance.get(`/qna/questions/${questionId}`);

      const q = res.data.question;

      const answersData = res.data.answers || [];

      // 디버깅: 답변 데이터 구조 확인
      if (answersData.length > 0) {
        console.log("답변 데이터 샘플:", answersData[0]);
      }

      set({
        question: q,
        answers: answersData,
        editForm: {
          title: q.title || "",
          body: q.body || "",
          isAnonymous: q.isAnonymous,
        },
      });
    } catch (e) {
      console.error("QnA 상세 조회 오류", e);
    } finally {
      set({ loading: false });
    }
  },

  /* ==============================
     수정 모드
  ============================== */
  setEditMode: (mode) => set({ editMode: mode }),

  setEditForm: (key, value) =>
    set((state) => ({
      editForm: {
        ...state.editForm,
        [key]: value,
      },
    })),

  /* ==============================
     질문 수정
     - 익명: anonymousPassword 필요
     - 비익명: 바로 수정
  ============================== */
  updateQuestion: async (questionId, anonymousPassword) => {
    const { editForm, question } = get();
    if (!questionId) return;

    const payload = {};

    if (editForm.title.trim()) payload.title = editForm.title.trim();
    if (editForm.body.trim()) payload.body = editForm.body.trim();
    payload.isAnonymous = editForm.isAnonymous;

    // ⭐ 익명 질문이면 비밀번호 포함
    if (question?.isAnonymous) {
      if (!anonymousPassword) {
        alert("비밀번호를 입력해주세요.");
        return;
      }
      payload.anonymousPassword = anonymousPassword;
    }

    try {
      await instance.put(`/qna/questions/${questionId}`, payload);
      set({ editMode: false });
      await get().loadDetail(questionId);
    } catch (e) {
      console.error("질문 수정 오류", e);
    }
  },

  /* ==============================
     질문 삭제
     - 익명: anonymousPassword 필요
     - 비익명: 바로 삭제
  ============================== */
  deleteQuestion: async (questionId, navigate, anonymousPassword) => {
    const { question } = get();
    if (!questionId) return;

    const config = {};

    // ⭐ 익명 질문이면 body에 비밀번호 포함
    if (question?.isAnonymous) {
      if (!anonymousPassword) {
        throw new Error("비밀번호를 입력해주세요.");
      }
      config.data = { anonymousPassword };
    }

    await instance.delete(`/qna/questions/${questionId}`, config);
    navigate("/qna");
  },

  /* ==============================
     답변
  ============================== */
  setNewAnswer: (text) => set({ newAnswer: text }),

  submitAnswer: async (questionId) => {
    const { newAnswer } = get();
    if (!questionId || !newAnswer.trim()) return;

    try {
      await instance.post("/qna/answers", {
        questionId,
        body: newAnswer.trim(),
        answerType: "USER",
      });

      set({ newAnswer: "" });
      await get().loadDetail(questionId);
    } catch (e) {
      console.error("답변 등록 오류", e);
    }
  },

  /* ==============================
     GPT 답변
  ============================== */
  createGptAnswer: async (questionId) => {
    if (!questionId) return;

    try {
      set({ loadingGPT: true });
      await instance.post(`/qna/questions/${questionId}/ai-answer`);
      await get().loadDetail(questionId);
    } catch (e) {
      console.error("GPT 답변 생성 오류", e);
    } finally {
      set({ loadingGPT: false });
    }
  },

  /* ==============================
     답변 Upvote
  ============================== */
  upvoteAnswer: async (questionId, answerId) => {
    if (!questionId || !answerId) return;

    const { answers } = get();
    const answerIndex = answers.findIndex((a) => a.id === answerId);

    if (answerIndex === -1) return;

    const currentAnswer = answers[answerIndex];
    const wasUpvoted = currentAnswer.userVote === "UP";

    try {
      const res = await instance.post(`/qna/answers/${answerId}/upvote`);

      // 백엔드 응답의 값을 그대로 사용 (계산하지 않음!)
      if (res.data) {
        // 약간의 딜레이를 주어 자연스럽게 업데이트
        await new Promise((resolve) => setTimeout(resolve, 200));

        const updatedAnswers = [...get().answers];
        const idx = updatedAnswers.findIndex((a) => a.id === answerId);
        if (idx !== -1) {
          // ✅ 백엔드가 계산한 score / userVote 만 사용
          updatedAnswers[idx] = {
            ...updatedAnswers[idx],
            score: res.data.score, // 최종 점수
            userVote: res.data.userVote, // null, 'UP', 'DOWN'
          };
          set({ answers: updatedAnswers });
        }
      } else {
        // 서버 응답이 없으면 전체 데이터 다시 로드
        await get().loadDetail(questionId);
      }

      // 서버 응답에서 최종 상태 확인
      const finalUserVote = res.data?.userVote ?? null;
      if (wasUpvoted && finalUserVote !== "UP") {
        toast.info("추천을 취소했습니다");
      } else if (!wasUpvoted && finalUserVote === "UP") {
        toast.success("추천했습니다");
      } else {
        if (wasUpvoted) {
          toast.info("추천을 취소했습니다");
        } else {
          toast.success("추천했습니다");
        }
      }
    } catch (e) {
      const errorMsg =
        e.response?.data?.message || e.message || "추천에 실패했습니다";

      if (e.response?.status === 401 || e.response?.status === 403) {
        toast.error("로그인이 필요합니다");
      } else if (e.response?.status === 400) {
        toast.error(errorMsg);
      } else {
        toast.error(errorMsg);
      }
      console.error("답변 추천 오류", e);
    }
  },

  /* ==============================
     답변 Downvote
  ============================== */
  downvoteAnswer: async (questionId, answerId) => {
    if (!questionId || !answerId) return;

    const { answers } = get();
    const answerIndex = answers.findIndex((a) => a.id === answerId);

    if (answerIndex === -1) return;

    const currentAnswer = answers[answerIndex];
    const wasDownvoted = currentAnswer.userVote === "DOWN";

    try {
      const res = await instance.post(`/qna/answers/${answerId}/downvote`);

      // 백엔드 응답의 값을 그대로 사용 (계산하지 않음!)
      if (res.data) {
        // 약간의 딜레이를 주어 자연스럽게 업데이트
        await new Promise((resolve) => setTimeout(resolve, 200));

        const updatedAnswers = [...get().answers];
        const idx = updatedAnswers.findIndex((a) => a.id === answerId);
        if (idx !== -1) {
          // ✅ 백엔드가 계산한 score / userVote 만 사용
          updatedAnswers[idx] = {
            ...updatedAnswers[idx],
            score: res.data.score, // 최종 점수
            userVote: res.data.userVote, // null, 'UP', 'DOWN'
          };
          set({ answers: updatedAnswers });
        }
      } else {
        // 서버 응답이 없으면 전체 데이터 다시 로드
        await get().loadDetail(questionId);
      }

      // 서버 응답에서 최종 상태 확인
      const finalUserVote = res.data?.userVote ?? null;
      if (wasDownvoted && finalUserVote !== "DOWN") {
        toast.info("비추천을 취소했습니다");
      } else if (!wasDownvoted && finalUserVote === "DOWN") {
        toast.error("비추천했습니다");
      } else {
        if (wasDownvoted) {
          toast.info("비추천을 취소했습니다");
        } else {
          toast.error("비추천했습니다");
        }
      }
    } catch (e) {
      const errorMsg =
        e.response?.data?.message || e.message || "비추천에 실패했습니다";

      if (e.response?.status === 401 || e.response?.status === 403) {
        toast.error("로그인이 필요합니다");
      } else if (e.response?.status === 400) {
        toast.error(errorMsg);
      } else {
        toast.error(errorMsg);
      }
      console.error("답변 비추천 오류", e);
    }
  },

  /* ==============================
     답변 채택/해제 (토글)
     - 익명 질문: anonymousPassword 필수
     - 일반 질문: 질문 작성자만 가능 (로그인 필요)
     - 이미 채택된 답변을 다시 호출하면 채택 해제
  ============================== */
  acceptAnswer: async (questionId, answerId, anonymousPassword) => {
    if (!questionId || !answerId) return;

    const { question } = get();

    if (!question) return;

    // ⭐ 백엔드 API 스펙에 맞게 anonymousPassword만 전송
    const payload = {};

    // 익명 질문인 경우 비밀번호 필수
    if (question.isAnonymous) {
      if (!anonymousPassword || anonymousPassword.trim() === "") {
        throw new Error("익명 질문의 답변을 채택하려면 비밀번호가 필요합니다");
      }
      payload.anonymousPassword = anonymousPassword.trim();
    }
    // 일반 질문인 경우 payload는 빈 객체 (또는 null) - 백엔드에서 required=false로 처리

    // 디버깅: 전송되는 payload 확인
    console.log("답변 채택 요청:", {
      answerId,
      questionId,
      isAnonymous: question.isAnonymous,
      payload,
      hasPassword: !!payload.anonymousPassword,
    });

    try {
      // 백엔드 API: POST /qna/answers/{answerId}/accept
      // 요청 body: QnaAnswerAcceptRequest { anonymousPassword? }
      // - 익명 질문: anonymousPassword 필수
      // - 일반 질문: 빈 객체 {} 또는 null (백엔드에서 required=false)

      const url = `/qna/answers/${answerId}/accept`;
      console.log("API 호출 시작:", {
        url: url,
        method: "POST",
        payload: payload,
        payloadString: JSON.stringify(payload),
        payloadKeys: Object.keys(payload),
        payloadSize: JSON.stringify(payload).length,
      });

      const response = await instance.post(url, payload);

      console.log("API 호출 성공:", {
        status: response.status,
        statusText: response.statusText,
        data: response.data,
      });

      // 채택/해제 후 전체 데이터 다시 로드
      await get().loadDetail(questionId);

      // 성공 메시지는 컴포넌트에서 처리 (토글 방식이므로 채택/해제 여부는 데이터 재로드로 확인)
    } catch (e) {
      // 디버깅: 에러 응답 상세 확인
      console.error("답변 채택/해제 오류 상세:", {
        status: e.response?.status,
        statusText: e.response?.statusText,
        data: e.response?.data,
        message: e.response?.data?.message,
        payload: payload,
      });

      const errorMsg =
        e.response?.data?.message ||
        e.response?.data?.error ||
        e.message ||
        "답변 채택/해제에 실패했습니다";

      // 403: 권한 없음 (비밀번호 오류, 작성자 아님 등)
      if (e.response?.status === 403) {
        // 비밀번호 관련 에러인지 확인
        if (
          errorMsg.includes("비밀번호") ||
          errorMsg.includes("일치") ||
          errorMsg.includes("password")
        ) {
          toast.error(
            "비밀번호가 일치하지 않습니다. 질문 작성 시 사용한 비밀번호를 확인해주세요."
          );
        } else {
          toast.error(errorMsg);
        }
      } else if (e.response?.status === 401) {
        toast.error("로그인이 필요합니다");
      } else if (e.response?.status === 400) {
        toast.error(errorMsg);
      } else {
        toast.error(errorMsg);
      }
      console.error("답변 채택/해제 오류", e);
      throw e; // 컴포넌트에서 에러 처리할 수 있도록 throw
    }
  },

  /* ==============================
     초기화
  ============================== */
  reset: () =>
    set({
      question: null,
      answers: [],
      newAnswer: "",
      loading: false,
      loadingGPT: false,
      editMode: false,
      editForm: { title: "", body: "", isAnonymous: false },
    }),
}));
