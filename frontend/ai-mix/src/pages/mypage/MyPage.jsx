// src/pages/MyPage.jsx
import { useEffect, useState, useMemo } from "react";
import axiosInstance from "../../api/axiosInstance";
import "../../styles/pages/mypage.css";
import { useNavigate } from "react-router-dom";
import { useUIStore } from "../../stores/ui.store";
import PasswordConfirmModal from "../../components/modal/PasswordConfirmModal";
import ToggleSwitch from "../../components/common/ToggleSwitch";
import {
  Calendar,
  User,
  LogIn,
  Cake,
  UserCircle,
  Key,
  Eye,
  EyeOff,
  Save,
  Info,
} from "lucide-react";
import ApiKeyGuideModal from "../../components/modal/ApiKeyGuideModal";
import TokenUsageGuideModal from "../../components/modal/TokenUsageGuideModal";
import { updateSettingsApi } from "../../api/auth";
import { toast } from "react-toastify";
import {
  LineChart,
  Line,
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts";
export default function MyPage() {
  const [data, setData] = useState(null);
  const [showPasswordModal, setShowPasswordModal] = useState(false);
  const [tokenGraphData, setTokenGraphData] = useState(null);
  const [tokenLoading, setTokenLoading] = useState(false);
  const [selectedPeriod, setSelectedPeriod] = useState("daily");
  const [selectedDays, setSelectedDays] = useState(30);
  const [apiKeyFilter, setApiKeyFilter] = useState("all"); // "all" | "user" | "shared"
  // GPT API 키 관련 상태
  const [apiKey, setApiKey] = useState("");
  const [showApiKey, setShowApiKey] = useState(false);
  const [isSavingApiKey, setIsSavingApiKey] = useState(false);
  const [apiKeyStatus, setApiKeyStatus] = useState({
    hasApiKey: false,
    maskedKey: null,
    lastValidatedAt: null,
  });
  const [apiKeyLoading, setApiKeyLoading] = useState(false);
  const [showApiKeyGuide, setShowApiKeyGuide] = useState(false);
  const [showTokenUsageGuide, setShowTokenUsageGuide] = useState(false);
  // zustand로 관리하는 로딩 상태
  const { mypageLoading, setMypageLoading } = useUIStore();
  const navigate = useNavigate();

  useEffect(() => {
    async function fetchData() {
      try {
        setMypageLoading(true);
        // ⭐ 쿠키 기반 인증 → 그냥 호출하면 자동 인증됨
        const res = await axiosInstance.get("/user/mypage");
        setData(res.data);
      } catch (err) {
        console.error("마이페이지 불러오기 실패:", err);

        // 인증 오류인 경우 로그인 페이지로 리다이렉트
        if (err.response?.status === 401 || err.response?.status === 403) {
          toast.error("로그인이 필요합니다.");
          navigate("/login");
          return;
        }

        // 다른 오류는 토스트 메시지 표시
        const errorMessage =
          err.response?.data?.message ||
          err.message ||
          "마이페이지를 불러올 수 없습니다.";
        toast.error(errorMessage);
      } finally {
        setMypageLoading(false);
      }
    }
    fetchData();
  }, [setMypageLoading, navigate]);

  // 토큰 사용량 그래프 데이터 가져오기
  useEffect(() => {
    async function fetchTokenGraphData() {
      try {
        setTokenLoading(true);
        const res = await axiosInstance.get(
          `/gpt/usage/graph?period=${selectedPeriod}&days=${selectedDays}`
        );
        console.log("토큰 사용량 API 응답:", res.data);
        setTokenGraphData(res.data);
      } catch (err) {
        console.error("토큰 사용량 그래프 불러오기 실패:", err);
        // 에러는 조용히 처리 (선택적 기능이므로)
      } finally {
        setTokenLoading(false);
      }
    }
    fetchTokenGraphData();
  }, [selectedPeriod, selectedDays]);

  // 저장된 API 키 상태 조회
  useEffect(() => {
    async function fetchApiKeyStatus() {
      try {
        setApiKeyLoading(true);
        const res = await axiosInstance.get("/user/api-key");
        setApiKeyStatus({
          hasApiKey: res.data.hasApiKey || false,
          maskedKey: res.data.maskedKey || null,
          lastValidatedAt: res.data.lastValidatedAt || null,
        });
      } catch (err) {
        console.error("API 키 상태 조회 실패:", err);
        // 에러는 조용히 처리 (선택적 기능이므로)
      } finally {
        setApiKeyLoading(false);
      }
    }
    fetchApiKeyStatus();
  }, []);

  // 차트 데이터 변환 (사용자 API 키와 공용 키 구분)
  // 새로운 API 구조: 각 날짜별로 userApiKeyTokens와 sharedApiKeyTokens 필드 제공
  const chartData = useMemo(() => {
    const rawData = tokenGraphData?.data;
    if (!rawData || !Array.isArray(rawData)) {
      console.log("토큰 데이터 없음:", tokenGraphData);
      return [];
    }

    // 날짜별로 사용자 키와 공용 키 사용량을 집계
    const dateMap = {};

    rawData.forEach((item, index) => {
      const date = item.date;

      // 첫 번째 아이템 디버깅
      if (index === 0) {
        console.log("첫 번째 데이터 아이템:", {
          date,
          totalTokens: item.totalTokens,
          userApiKeyTokens: item.userApiKeyTokens,
          sharedApiKeyTokens: item.sharedApiKeyTokens,
          hasByType: !!item.byType,
        });
      }

      if (!dateMap[date]) {
        dateMap[date] = {
          date,
          내API키: 0,
          공용키: 0,
          총토큰: item.totalTokens || 0,
          // 타입별 사용량도 구분
          채팅_내키: 0,
          채팅_공용: 0,
          QnA_내키: 0,
          QnA_공용: 0,
          배틀문제_내키: 0,
          배틀문제_공용: 0,
          배틀채점_내키: 0,
          배틀채점_공용: 0,
          지식카드_내키: 0,
          지식카드_공용: 0,
        };
      }

      // 새로운 API 구조: userApiKeyTokens와 sharedApiKeyTokens 필드 사용
      if (
        item.userApiKeyTokens !== undefined ||
        item.sharedApiKeyTokens !== undefined
      ) {
        const userKeyTokens = item.userApiKeyTokens || 0;
        const sharedKeyTokens = item.sharedApiKeyTokens || 0;

        dateMap[date].내API키 += userKeyTokens;
        dateMap[date].공용키 += sharedKeyTokens;

        // byType이 객체이고 내부에 타입별 사용량이 있는 경우
        if (item.byType && typeof item.byType === "object") {
          Object.keys(item.byType).forEach((type) => {
            const typeData = item.byType[type];

            // byType 내부에 userApiKey/sharedApiKey로 구분된 경우
            if (typeof typeData === "object" && typeData !== null) {
              const typeUserKey = typeData.userApiKey || 0;
              const typeSharedKey = typeData.sharedApiKey || 0;

              // 타입별로 집계
              if (type === "CHAT") {
                dateMap[date].채팅_내키 += typeUserKey;
                dateMap[date].채팅_공용 += typeSharedKey;
              } else if (type === "QNA") {
                dateMap[date].QnA_내키 += typeUserKey;
                dateMap[date].QnA_공용 += typeSharedKey;
              } else if (type === "BATTLE_QUESTION") {
                dateMap[date].배틀문제_내키 += typeUserKey;
                dateMap[date].배틀문제_공용 += typeSharedKey;
              } else if (type === "BATTLE_SCORING") {
                dateMap[date].배틀채점_내키 += typeUserKey;
                dateMap[date].배틀채점_공용 += typeSharedKey;
              } else if (type === "KNOWLEDGE_CARD") {
                dateMap[date].지식카드_내키 += typeUserKey;
                dateMap[date].지식카드_공용 += typeSharedKey;
              }
            } else {
              // 숫자로만 되어 있는 경우 - userApiKeyTokens와 sharedApiKeyTokens 비율로 분배
              const typeTotal = typeData || 0;
              const totalForDay = userKeyTokens + sharedKeyTokens;
              if (totalForDay > 0) {
                const userRatio = userKeyTokens / totalForDay;
                const sharedRatio = sharedKeyTokens / totalForDay;
                const typeUserKey = Math.round(typeTotal * userRatio);
                const typeSharedKey = typeTotal - typeUserKey;

                // 타입별로 집계
                if (type === "CHAT") {
                  dateMap[date].채팅_내키 += typeUserKey;
                  dateMap[date].채팅_공용 += typeSharedKey;
                } else if (type === "QNA") {
                  dateMap[date].QnA_내키 += typeUserKey;
                  dateMap[date].QnA_공용 += typeSharedKey;
                } else if (type === "BATTLE_QUESTION") {
                  dateMap[date].배틀문제_내키 += typeUserKey;
                  dateMap[date].배틀문제_공용 += typeSharedKey;
                } else if (type === "BATTLE_SCORING") {
                  dateMap[date].배틀채점_내키 += typeUserKey;
                  dateMap[date].배틀채점_공용 += typeSharedKey;
                } else if (type === "KNOWLEDGE_CARD") {
                  dateMap[date].지식카드_내키 += typeUserKey;
                  dateMap[date].지식카드_공용 += typeSharedKey;
                }
              } else {
                // 비율 계산 불가 시 공용 키로 처리
                if (type === "CHAT") {
                  dateMap[date].채팅_공용 += typeTotal;
                } else if (type === "QNA") {
                  dateMap[date].QnA_공용 += typeTotal;
                } else if (type === "BATTLE_QUESTION") {
                  dateMap[date].배틀문제_공용 += typeTotal;
                } else if (type === "BATTLE_SCORING") {
                  dateMap[date].배틀채점_공용 += typeTotal;
                } else if (type === "KNOWLEDGE_CARD") {
                  dateMap[date].지식카드_공용 += typeTotal;
                }
              }
            }
          });
        }
      } else {
        // 하위 호환성: 기존 구조 (userApiKeyTokens/sharedApiKeyTokens가 없는 경우)
        // 전체를 공용 키로 표시
        const total = item.totalTokens || 0;
        dateMap[date].공용키 += total;
      }
    });

    const result = Object.values(dateMap);
    console.log("차트 데이터 변환 결과:", {
      총데이터수: result.length,
      첫번째데이터: result[0],
      내API키합계: result.reduce((sum, item) => sum + (item.내API키 || 0), 0),
      공용키합계: result.reduce((sum, item) => sum + (item.공용키 || 0), 0),
    });
    return result;
  }, [tokenGraphData]);

  // 날짜 포맷팅 (MM-DD)
  const formatDate = (dateStr) => {
    const date = new Date(dateStr);
    return `${String(date.getMonth() + 1).padStart(2, "0")}-${String(
      date.getDate()
    ).padStart(2, "0")}`;
  };

  // 기간별 날짜 포맷팅 (툴팁용)
  const formatDateForTooltip = (dateStr, period) => {
    const date = new Date(dateStr);
    if (period === "daily") {
      return date.toISOString().split("T")[0]; // YYYY-MM-DD
    } else if (period === "weekly") {
      // 주의 시작일과 종료일 계산
      const dayOfWeek = date.getDay();
      const startDate = new Date(date);
      startDate.setDate(date.getDate() - dayOfWeek);
      const endDate = new Date(startDate);
      endDate.setDate(startDate.getDate() + 6);
      return `${startDate.toISOString().split("T")[0]} ~ ${endDate.toISOString().split("T")[0]}`;
    } else if (period === "monthly") {
      return `${date.getFullYear()}년 ${date.getMonth() + 1}월`;
    }
    return dateStr;
  };

  // 커스텀 툴팁 컴포넌트
  const CustomTooltip = ({ active, payload, label }) => {
    if (!active || !payload || !payload.length) return null;

    // 원본 데이터 찾기 (formattedChartData에서 label과 date가 일치하는 것)
    const dataPoint = formattedChartData.find((item) => item.date === label);

    if (!dataPoint) return null;

    // 원본 날짜 사용
    const originalDate = dataPoint.originalDate || dataPoint.date;
    const dateLabel = formatDateForTooltip(originalDate, selectedPeriod);

    // 사용 유형별 토큰 계산
    const getTypeTokens = (type, apiKeyType) => {
      if (apiKeyFilter === "all") {
        // 전체 필터일 때는 내 키와 공용 키 모두 표시
        if (apiKeyType === "user") {
          switch (type) {
            case "CHAT":
              return dataPoint.채팅_내키 || 0;
            case "QNA":
              return dataPoint.QnA_내키 || 0;
            case "BATTLE_QUESTION":
              return dataPoint.배틀문제_내키 || 0;
            case "BATTLE_SCORING":
              return dataPoint.배틀채점_내키 || 0;
            case "KNOWLEDGE_CARD":
              return dataPoint.지식카드_내키 || 0;
            default:
              return 0;
          }
        } else {
          switch (type) {
            case "CHAT":
              return dataPoint.채팅_공용 || 0;
            case "QNA":
              return dataPoint.QnA_공용 || 0;
            case "BATTLE_QUESTION":
              return dataPoint.배틀문제_공용 || 0;
            case "BATTLE_SCORING":
              return dataPoint.배틀채점_공용 || 0;
            case "KNOWLEDGE_CARD":
              return dataPoint.지식카드_공용 || 0;
            default:
              return 0;
          }
        }
      } else {
        // 필터가 적용된 경우
        switch (type) {
          case "CHAT":
            return dataPoint.채팅 || 0;
          case "QNA":
            return dataPoint.QnA || 0;
          case "BATTLE_QUESTION":
            return dataPoint.배틀문제 || 0;
          case "BATTLE_SCORING":
            return dataPoint.배틀채점 || 0;
          case "KNOWLEDGE_CARD":
            return dataPoint.지식카드 || 0;
          default:
            return 0;
        }
      }
    };

    const userKeyTotal = dataPoint.내API키 || 0;
    const sharedKeyTotal = dataPoint.공용키 || 0;
    const totalTokens = dataPoint.총토큰 || userKeyTotal + sharedKeyTotal;

    return (
      <div className="token-usage-tooltip">
        <div className="tooltip-header">
          <div className="tooltip-date">{dateLabel}</div>
          <div className="tooltip-total">총 토큰: {totalTokens.toLocaleString()}</div>
        </div>

        {apiKeyFilter === "all" ? (
          <>
            {/* 내 API 키 섹션 */}
            <div className="tooltip-section tooltip-section-user">
              <div className="tooltip-section-title">내 API 키</div>
              <div className="tooltip-usage-items">
                <div className="tooltip-usage-item">
                  <span className="tooltip-label">QNA</span>
                  <span className="tooltip-value">
                    {getTypeTokens("QNA", "user").toLocaleString()}
                  </span>
                </div>
                <div className="tooltip-usage-item">
                  <span className="tooltip-label">배틀문제</span>
                  <span className="tooltip-value">
                    {getTypeTokens("BATTLE_QUESTION", "user").toLocaleString()}
                  </span>
                </div>
                <div className="tooltip-usage-item">
                  <span className="tooltip-label">배틀 채점</span>
                  <span className="tooltip-value">
                    {getTypeTokens("BATTLE_SCORING", "user").toLocaleString()}
                  </span>
                </div>
                <div className="tooltip-usage-item">
                  <span className="tooltip-label">지식 카드</span>
                  <span className="tooltip-value">
                    {getTypeTokens("KNOWLEDGE_CARD", "user").toLocaleString()}
                  </span>
                </div>
                <div className="tooltip-usage-item">
                  <span className="tooltip-label">채팅</span>
                  <span className="tooltip-value">
                    {getTypeTokens("CHAT", "user").toLocaleString()}
                  </span>
                </div>
                <div className="tooltip-usage-item tooltip-usage-total">
                  <span className="tooltip-label">총 토큰</span>
                  <span className="tooltip-value">{userKeyTotal.toLocaleString()}</span>
                </div>
              </div>
            </div>

            {/* 공용 키 섹션 */}
            <div className="tooltip-section tooltip-section-shared">
              <div className="tooltip-section-title">공용 키</div>
              <div className="tooltip-usage-items">
                <div className="tooltip-usage-item">
                  <span className="tooltip-label">QNA</span>
                  <span className="tooltip-value">
                    {getTypeTokens("QNA", "shared").toLocaleString()}
                  </span>
                </div>
                <div className="tooltip-usage-item">
                  <span className="tooltip-label">배틀문제</span>
                  <span className="tooltip-value">
                    {getTypeTokens("BATTLE_QUESTION", "shared").toLocaleString()}
                  </span>
                </div>
                <div className="tooltip-usage-item">
                  <span className="tooltip-label">배틀 채점</span>
                  <span className="tooltip-value">
                    {getTypeTokens("BATTLE_SCORING", "shared").toLocaleString()}
                  </span>
                </div>
                <div className="tooltip-usage-item">
                  <span className="tooltip-label">지식 카드</span>
                  <span className="tooltip-value">
                    {getTypeTokens("KNOWLEDGE_CARD", "shared").toLocaleString()}
                  </span>
                </div>
                <div className="tooltip-usage-item">
                  <span className="tooltip-label">채팅</span>
                  <span className="tooltip-value">
                    {getTypeTokens("CHAT", "shared").toLocaleString()}
                  </span>
                </div>
                <div className="tooltip-usage-item tooltip-usage-total">
                  <span className="tooltip-label">총 토큰</span>
                  <span className="tooltip-value">{sharedKeyTotal.toLocaleString()}</span>
                </div>
              </div>
            </div>
          </>
        ) : (
          <div className="tooltip-section">
            <div className="tooltip-section-title">
              {apiKeyFilter === "user" ? "내 API 키" : "공용 키"}
            </div>
            <div className="tooltip-usage-items">
              <div className="tooltip-usage-item">
                <span className="tooltip-label">QNA</span>
                <span className="tooltip-value">
                  {getTypeTokens("QNA").toLocaleString()}
                </span>
              </div>
              <div className="tooltip-usage-item">
                <span className="tooltip-label">배틀문제</span>
                <span className="tooltip-value">
                  {getTypeTokens("BATTLE_QUESTION").toLocaleString()}
                </span>
              </div>
              <div className="tooltip-usage-item">
                <span className="tooltip-label">배틀 채점</span>
                <span className="tooltip-value">
                  {getTypeTokens("BATTLE_SCORING").toLocaleString()}
                </span>
              </div>
              <div className="tooltip-usage-item">
                <span className="tooltip-label">지식 카드</span>
                <span className="tooltip-value">
                  {getTypeTokens("KNOWLEDGE_CARD").toLocaleString()}
                </span>
              </div>
              <div className="tooltip-usage-item">
                <span className="tooltip-label">채팅</span>
                <span className="tooltip-value">
                  {getTypeTokens("CHAT").toLocaleString()}
                </span>
              </div>
              <div className="tooltip-usage-item tooltip-usage-total">
                <span className="tooltip-label">총 토큰</span>
                <span className="tooltip-value">{totalTokens.toLocaleString()}</span>
              </div>
            </div>
          </div>
        )}
      </div>
    );
  };

  // 필터링된 차트 데이터
  const formattedChartData = useMemo(() => {
    let filtered = chartData.map((item) => ({
      ...item,
      date: formatDate(item.date),
      originalDate: item.date, // 원본 날짜 유지
    }));

    // API 키 필터 적용
    if (apiKeyFilter === "user") {
      // 내 API 키만 표시
      filtered = filtered.map((item) => ({
        ...item,
        총토큰: item.내API키 || 0,
        채팅: item.채팅_내키 || 0,
        QnA: item.QnA_내키 || 0,
        배틀문제: item.배틀문제_내키 || 0,
        배틀채점: item.배틀채점_내키 || 0,
        지식카드: item.지식카드_내키 || 0,
      }));
      console.log("내 API 키 필터 적용:", {
        필터: apiKeyFilter,
        필터링된데이터수: filtered.length,
        첫번째데이터: filtered[0],
        총토큰합계: filtered.reduce((sum, item) => sum + (item.총토큰 || 0), 0),
      });
    } else if (apiKeyFilter === "shared") {
      // 공용 키만 표시
      filtered = filtered.map((item) => ({
        ...item,
        총토큰: item.공용키 || 0,
        채팅: item.채팅_공용 || 0,
        QnA: item.QnA_공용 || 0,
        배틀문제: item.배틀문제_공용 || 0,
        배틀채점: item.배틀채점_공용 || 0,
        지식카드: item.지식카드_공용 || 0,
      }));
    }
    // "all" 필터일 때는 내API키와 공용키를 별도로 유지하여 그래프에서 구분 표시

    return filtered;
  }, [chartData, apiKeyFilter]);

  if (mypageLoading)
    return <div className="mypage-loading">불러오는 중...</div>;
  if (!data) return <div className="mypage-error">데이터가 없습니다.</div>;

  // 새로운 API 응답 구조: userResponse, statistics
  const { userResponse, statistics } = data;
  const userInfo = userResponse;
  const settings = userResponse?.settings;

  // userInfo가 없으면 에러 표시
  if (!userInfo) {
    return (
      <div className="mypage-error">
        <p>사용자 정보를 불러올 수 없습니다.</p>
      </div>
    );
  }

  const getAvatarUrl = (url) => {
    if (!url) return null;
    if (url.startsWith("http://") || url.startsWith("https://")) {
      return url;
    }
    return url.startsWith("/") ? url : `/${url}`;
  };

  const avatarSrc = getAvatarUrl(userInfo?.avatarUrl);

  const handlePasswordConfirm = (password) => {
    // 비밀번호 확인 성공 후 회원정보 수정 페이지로 이동
    setShowPasswordModal(false);
    navigate("/mypage/edit");
  };

  // 다크모드 토글 핸들러
  const handleDarkModeToggle = async (checked) => {
    try {
      await updateSettingsApi({ darkMode: checked });
      // 로컬 상태 업데이트 (새로운 구조: userResponse.settings)
      setData((prev) => ({
        ...prev,
        userResponse: {
          ...prev.userResponse,
          settings: { ...prev.userResponse?.settings, darkMode: checked },
        },
      }));
      toast.success(`다크모드가 ${checked ? "켜졌습니다" : "꺼졌습니다"}`);
    } catch (err) {
      console.error("다크모드 설정 업데이트 실패:", err);
      toast.error("설정 업데이트에 실패했습니다.");
    }
  };

  // 알림 토글 핸들러
  const handleNotificationsToggle = async (checked) => {
    try {
      await updateSettingsApi({ notifications: checked });
      // 로컬 상태 업데이트 (새로운 구조: userResponse.settings)
      setData((prev) => ({
        ...prev,
        userResponse: {
          ...prev.userResponse,
          settings: { ...prev.userResponse?.settings, notifications: checked },
        },
      }));
      toast.success(`알림이 ${checked ? "켜졌습니다" : "꺼졌습니다"}`);
    } catch (err) {
      console.error("알림 설정 업데이트 실패:", err);
      toast.error("설정 업데이트에 실패했습니다.");
    }
  };

  // GPT API 키 저장 핸들러
  const handleSaveApiKey = async () => {
    if (!apiKey.trim()) {
      toast.error("API 키를 입력해주세요.");
      return;
    }

    // API 키 형식 간단 검증 (sk-로 시작하는지 확인)
    if (!apiKey.startsWith("sk-")) {
      toast.warning(
        "올바른 OpenAI API 키 형식이 아닙니다. (sk-로 시작해야 합니다)"
      );
      return;
    }

    setIsSavingApiKey(true);
    try {
      const res = await axiosInstance.put("/user/api-key", { apiKey });

      // 응답 데이터로 상태 업데이트
      setApiKeyStatus({
        hasApiKey: res.data.hasApiKey || false,
        maskedKey: res.data.maskedKey || null,
        lastValidatedAt: res.data.lastValidatedAt || null,
      });

      toast.success("API 키가 저장되었습니다.");
      setApiKey(""); // 입력 필드 초기화
    } catch (err) {
      console.error("API 키 저장 실패:", err);
      // 에러 메시지는 axiosInstance의 인터셉터에서 자동으로 표시됨
      // 추가 에러 처리가 필요한 경우 여기서 처리
    } finally {
      setIsSavingApiKey(false);
    }
  };

  // GPT API 키 삭제 핸들러
  const handleDeleteApiKey = async () => {
    if (!window.confirm("API 키를 삭제하시겠습니까?")) {
      return;
    }

    setIsSavingApiKey(true);
    try {
      await axiosInstance.delete("/user/api-key");

      // 상태 초기화
      setApiKeyStatus({
        hasApiKey: false,
        maskedKey: null,
        lastValidatedAt: null,
      });

      toast.success("API 키가 삭제되었습니다.");
    } catch (err) {
      console.error("API 키 삭제 실패:", err);
      // 에러 메시지는 axiosInstance의 인터셉터에서 자동으로 표시됨
    } finally {
      setIsSavingApiKey(false);
    }
  };

  return (
    <div className="mypage-wrapper">
      {/* 프로필 카드 */}
      <div className="profile-card">
        <button
          className="edit-profile-btn"
          onClick={() => setShowPasswordModal(true)}
        >
          회원정보수정
        </button>
        <div className="profile-top">
          {avatarSrc ? (
            <img src={avatarSrc} alt="avatar" className="profile-avatar" />
          ) : (
            <div className="profile-placeholder">
              {userInfo?.nickname?.[0]?.toUpperCase() || "U"}
            </div>
          )}

          <div className="profile-basic">
            <h2 className="profile-name">{userInfo?.nickname || "사용자"}</h2>
            <p className="profile-email">{userInfo?.email || ""}</p>
            <div className="profile-bio">
              <div className="profile-bio-header">
                <UserCircle size={18} />
                <span className="profile-bio-label">자기소개</span>
              </div>
              <p className="profile-bio-text">
                {userInfo?.bio || "소개글 없음"}
              </p>
            </div>
          </div>
        </div>

        <div className="profile-dates">
          <div className="profile-date-item">
            <Calendar size={16} />
            <span>
              가입일:{" "}
              {userInfo?.createdAt ? userInfo.createdAt.split("T")[0] : "N/A"}
            </span>
          </div>
          <div className="profile-date-item">
            <LogIn size={16} />
            <span>
              최근 로그인:{" "}
              {userInfo?.lastLoginAt
                ? userInfo.lastLoginAt.split("T")[0]
                : "N/A"}
            </span>
          </div>
          <div className="profile-date-item">
            <Cake size={16} />
            <span>생년월일: {userInfo?.birthDate || "N/A"}</span>
          </div>
        </div>
      </div>

      {/* 활동 통계 */}
      {statistics && (
        <div className="stats-card">
          <h3>활동 통계</h3>

          <div className="stats-grid">
            {/* ❗ 새 페이지로 이동 */}
            <div
              className="stat-box stat-box-clickable"
              onClick={() => navigate("/mypage/battle-history")}
            >
              <span className="stat-value">{statistics?.battleCount || 0}</span>
              <span className="stat-label">배틀 참여</span>
            </div>

            <div
              className="stat-box stat-box-clickable"
              onClick={() => navigate("/chat")}
            >
              <span className="stat-value">
                {statistics?.chatSessionCount || 0}
              </span>
              <span className="stat-label">개의 채팅</span>
            </div>
          </div>
        </div>
      )}

      {/* GPT 토큰 사용량 그래프 */}
      <div className="token-usage-card">
        <div className="token-usage-header">
          <div className="token-usage-title-wrapper">
            <h3>GPT 토큰 사용량</h3>
            <button
              className="token-usage-info-btn"
              onClick={() => setShowTokenUsageGuide(true)}
              title="GPT 토큰 사용량 안내"
            >
              <Info size={18} />
            </button>
          </div>
          {tokenGraphData?.summary && (
            <div className="token-summary">
              <div className="token-summary-item">
                <span className="token-summary-label">오늘</span>
                <span className="token-summary-value">
                  {tokenGraphData.summary.todayTotalTokens?.toLocaleString() ||
                    0}
                </span>
              </div>
              <div className="token-summary-item">
                <span className="token-summary-label">전체</span>
                <span className="token-summary-value">
                  {tokenGraphData.summary.totalTokens?.toLocaleString() || 0}
                </span>
              </div>
              <div className="token-summary-item">
                <span className="token-summary-label">평균/일</span>
                <span className="token-summary-value">
                  {Math.round(
                    tokenGraphData.summary.averageDailyTokens || 0
                  ).toLocaleString()}
                </span>
              </div>
              {/* 새로운 API 구조: 사용자 API 키와 공용 키 사용량 표시 */}
              {tokenGraphData.summary.userApiKeyTokens !== undefined && (
                <div className="token-summary-item">
                  <span className="token-summary-label">내 API 키</span>
                  <span className="token-summary-value">
                    {tokenGraphData.summary.userApiKeyTokens?.toLocaleString() ||
                      0}
                  </span>
                </div>
              )}
              {tokenGraphData.summary.sharedApiKeyTokens !== undefined && (
                <div className="token-summary-item">
                  <span className="token-summary-label">공용 키</span>
                  <span className="token-summary-value">
                    {tokenGraphData.summary.sharedApiKeyTokens?.toLocaleString() ||
                      0}
                  </span>
                </div>
              )}
            </div>
          )}
        </div>

        {/* API 키 필터 버튼 */}
        <div className="token-api-key-filter">
          <button
            className={`api-key-filter-btn ${
              apiKeyFilter === "all" ? "active" : ""
            }`}
            onClick={() => setApiKeyFilter("all")}
            title="전체 API 키 사용량을 확인할 수 있습니다."
          >
            전체
          </button>
          <button
            className={`api-key-filter-btn ${
              apiKeyFilter === "user" ? "active" : ""
            }`}
            onClick={() => setApiKeyFilter("user")}
            title="💡 내 API 키: 등록하신 개인 OpenAI API 키로 호출한 토큰 사용량입니다."
          >
            내 API 키
          </button>
          <button
            className={`api-key-filter-btn ${
              apiKeyFilter === "shared" ? "active" : ""
            }`}
            onClick={() => setApiKeyFilter("shared")}
            title="💡 공용 키: 시스템 기본 API 키로 호출한 토큰 사용량입니다."
          >
            공용 키
          </button>
        </div>

        <div className="token-period-selector">
          <button
            className={`period-btn ${
              selectedPeriod === "daily" ? "active" : ""
            }`}
            onClick={() => {
              setSelectedPeriod("daily");
              setSelectedDays(30);
            }}
            title="💡 일별: 최근 30일간의 일별 사용량을 확인할 수 있습니다."
          >
            일별 (30일)
          </button>
          <button
            className={`period-btn ${
              selectedPeriod === "weekly" ? "active" : ""
            }`}
            onClick={() => {
              setSelectedPeriod("weekly");
              setSelectedDays(84);
            }}
            title="💡 주별: 최근 12주간의 주별 사용량을 확인할 수 있습니다."
          >
            주별 (12주)
          </button>
          <button
            className={`period-btn ${
              selectedPeriod === "monthly" ? "active" : ""
            }`}
            onClick={() => {
              setSelectedPeriod("monthly");
              setSelectedDays(365);
            }}
            title="💡 월별: 최근 12개월간의 월별 사용량을 확인할 수 있습니다."
          >
            월별 (12개월)
          </button>
        </div>

        {tokenLoading ? (
          <div className="token-chart-loading">그래프 불러오는 중...</div>
        ) : formattedChartData && formattedChartData.length > 0 ? (
          <div className="token-chart-container">
            <ResponsiveContainer width="100%" height={400}>
              <AreaChart data={formattedChartData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#e8eafe" />
                <XAxis
                  dataKey="date"
                  stroke="#666"
                  style={{ fontSize: "12px" }}
                  angle={-45}
                  textAnchor="end"
                  height={80}
                />
                <YAxis
                  stroke="#666"
                  style={{ fontSize: "12px" }}
                  tickFormatter={(value) => {
                    if (value >= 1000000)
                      return `${(value / 1000000).toFixed(1)}M`;
                    if (value >= 1000) return `${(value / 1000).toFixed(1)}K`;
                    return value;
                  }}
                />
                <Tooltip content={<CustomTooltip />} />
                <Legend wrapperStyle={{ paddingTop: "20px" }} iconType="line" />
                {apiKeyFilter === "all" ? (
                  <>
                    {/* 전체 필터일 때: 내 API 키와 공용 키를 구분해서 표시 */}
                    <Area
                      type="monotone"
                      dataKey="내API키"
                      stackId="user"
                      stroke="#4CAF50"
                      fill="#4CAF50"
                      fillOpacity={0.6}
                      name="내 API 키"
                    />
                    <Area
                      type="monotone"
                      dataKey="공용키"
                      stackId="shared"
                      stroke="#2196F3"
                      fill="#2196F3"
                      fillOpacity={0.6}
                      name="공용 키"
                    />
                  </>
                ) : (
                  <>
                    {/* 필터가 적용된 경우: 타입별로 표시 */}
                    <Area
                      type="monotone"
                      dataKey="총토큰"
                      stroke="#7d5cf6"
                      strokeWidth={2}
                      fill="#7d5cf6"
                      fillOpacity={0.2}
                      name="총 토큰"
                    />
                    <Area
                      type="monotone"
                      dataKey="채팅"
                      stackId="1"
                      stroke="#4caf50"
                      fill="#4caf50"
                      fillOpacity={0.6}
                      name="채팅"
                    />
                    <Area
                      type="monotone"
                      dataKey="QnA"
                      stackId="1"
                      stroke="#2196f3"
                      fill="#2196f3"
                      fillOpacity={0.6}
                      name="QnA"
                    />
                    <Area
                      type="monotone"
                      dataKey="배틀문제"
                      stackId="1"
                      stroke="#ff9800"
                      fill="#ff9800"
                      fillOpacity={0.6}
                      name="배틀 문제"
                    />
                    <Area
                      type="monotone"
                      dataKey="배틀채점"
                      stackId="1"
                      stroke="#f44336"
                      fill="#f44336"
                      fillOpacity={0.6}
                      name="배틀 채점"
                    />
                    <Area
                      type="monotone"
                      dataKey="지식카드"
                      stackId="1"
                      stroke="#9c27b0"
                      fill="#9c27b0"
                      fillOpacity={0.6}
                      name="지식 카드"
                    />
                  </>
                )}
              </AreaChart>
            </ResponsiveContainer>
          </div>
        ) : (
          <div className="token-chart-empty">사용량 데이터가 없습니다.</div>
        )}
      </div>

      {/* GPT API 키 설정 */}
      <div className="api-key-card">
        <div className="api-key-header">
          <div className="api-key-title">
            <Key size={20} />
            <h3>GPT API 키 설정</h3>
          </div>
          <button
            className="api-key-info-btn"
            onClick={() => setShowApiKeyGuide(true)}
            aria-label="API 키 안내"
            title="API 키 발급 및 사용 안내"
          >
            <Info size={18} />
          </button>
        </div>

        <div className="api-key-description">
          <p>
            사용량 절감을 위해 본인의 OpenAI API 키를 입력하실 수 있습니다.
            <br />
            키는 안전하게 암호화되어 저장됩니다.
            <br />
            <span className="api-key-model-info">
              사용 모델: <strong>gpt-4o-mini</strong>
            </span>
          </p>
        </div>

        <div className="api-key-input-wrapper">
          <div className="api-key-input-group">
            <input
              type={showApiKey ? "text" : "password"}
              className="api-key-input"
              placeholder="sk-..."
              value={apiKey}
              onChange={(e) => setApiKey(e.target.value)}
              disabled={isSavingApiKey}
            />
            <button
              type="button"
              className="api-key-toggle-btn"
              onClick={() => setShowApiKey(!showApiKey)}
              aria-label={showApiKey ? "키 숨기기" : "키 보기"}
            >
              {showApiKey ? <EyeOff size={18} /> : <Eye size={18} />}
            </button>
          </div>

          <button
            className="api-key-save-btn"
            onClick={handleSaveApiKey}
            disabled={isSavingApiKey || !apiKey.trim()}
          >
            <Save size={16} />
            {isSavingApiKey ? "저장 중..." : "저장"}
          </button>
        </div>

        {apiKeyLoading ? (
          <div className="api-key-status">
            <span className="api-key-status-label">로딩 중...</span>
          </div>
        ) : apiKeyStatus.hasApiKey && apiKeyStatus.maskedKey ? (
          <div className="api-key-status">
            <span className="api-key-status-label">현재 저장된 키:</span>
            <span className="api-key-status-value">
              {apiKeyStatus.maskedKey}
            </span>
            <button
              className="api-key-remove-btn"
              onClick={handleDeleteApiKey}
              disabled={isSavingApiKey}
            >
              {isSavingApiKey ? "삭제 중..." : "삭제"}
            </button>
          </div>
        ) : null}
      </div>

      {/* 설정 */}
      {settings && (
        <div className="settings-card">
          <h3>사용자 설정</h3>

          <div className="setting-item">
            <span>다크모드</span>
            <ToggleSwitch
              checked={settings?.darkMode || false}
              onChange={(checked) => {
                handleDarkModeToggle(checked);
              }}
            />
          </div>

          <div className="setting-item">
            <span>알림</span>
            <ToggleSwitch
              checked={settings?.notifications || false}
              onChange={(checked) => {
                handleNotificationsToggle(checked);
              }}
            />
          </div>
        </div>
      )}

      {/* 비밀번호 확인 모달 */}
      {showPasswordModal && (
        <PasswordConfirmModal
          onClose={() => setShowPasswordModal(false)}
          onConfirm={handlePasswordConfirm}
        />
      )}

      {/* API 키 안내 모달 */}
      {showApiKeyGuide && (
        <ApiKeyGuideModal onClose={() => setShowApiKeyGuide(false)} />
      )}

      {/* 토큰 사용량 안내 모달 */}
      {showTokenUsageGuide && (
        <TokenUsageGuideModal onClose={() => setShowTokenUsageGuide(false)} />
      )}
    </div>
  );
}
