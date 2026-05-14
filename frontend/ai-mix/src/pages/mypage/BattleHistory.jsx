import { useEffect, useState } from "react";
import axiosInstance from "../../api/axiosInstance";
import "../../styles/pages/battle-history.css";
import {
  Trophy,
  Target,
  Zap,
  Clock,
  ChevronDown,
  ChevronUp,
  RotateCcw,
} from "lucide-react";
import { useNavigate } from "react-router-dom";
import BattleModal from "../../components/battle/BattleModal";
import { toast } from "react-toastify";

export default function BattleHistory() {
  const [data, setData] = useState(null);
  const [filter, setFilter] = useState(null); // null: 전체, 'WIN', 'LOSE', 'DRAW', 'IN_PROGRESS'
  const [dateFilter, setDateFilter] = useState(null); // null: 전체, 날짜 문자열 또는 'this_week', 'last_week' 등
  const [isDateFilterOpen, setIsDateFilterOpen] = useState(false); // 날짜 필터 펼침/접힘 상태
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;
  const navigate = useNavigate();
  const [resumeBattleId, setResumeBattleId] = useState(null);
  const [resumeBattleData, setResumeBattleData] = useState(null);
  const [showResumeModal, setShowResumeModal] = useState(false);

  useEffect(() => {
    async function fetchData() {
      try {
        // ⭐ 쿠키 기반 인증 → 자동 refresh 포함
        const res = await axiosInstance.get("/battle/history");
        setData(res.data);
      } catch (err) {
        if (err.response?.status === 401 || err.response?.status === 403) {
          toast.error("로그인이 필요합니다.");
          navigate("/login");
          return;
        }

        console.error("배틀 전적 불러오기 실패:", err);
      }
    }
    fetchData();
  }, [navigate]);

  /* -----------------------------
     상대 시간 계산
  ----------------------------- */
  const getRelativeTime = (dateString) => {
    const now = new Date();
    const date = new Date(dateString);
    const diffMs = now.getTime() - date.getTime();

    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));
    const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
    const diffMinutes = Math.floor(diffMs / (1000 * 60));

    if (diffDays > 0) return `${diffDays}일 전`;
    if (diffHours > 0) return `${diffHours}시간 전`;
    if (diffMinutes > 0) return `${diffMinutes}분 전`;
    return "방금 전";
  };

  /* -----------------------------
     날짜별 그룹화
  ----------------------------- */
  const groupBattlesByDate = (battles) => {
    const groups = {};

    battles.forEach((battle) => {
      const date = new Date(battle.createdAt);
      const dateKey = date.toLocaleDateString("ko-KR", {
        year: "numeric",
        month: "long",
        day: "numeric",
      });

      if (!groups[dateKey]) groups[dateKey] = [];
      groups[dateKey].push(battle);
    });

    return groups;
  };

  if (!data) return <div className="bh-loading">불러오는 중...</div>;

  const { statistics } = data;

  /* -----------------------------
     날짜 목록 추출
  ----------------------------- */
  const getDateKey = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString("ko-KR", {
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  };

  // 날짜별로 그룹화하여 정렬
  const dateMap = new Map();
  data.battles.forEach((battle) => {
    const dateKey = getDateKey(battle.createdAt);
    if (!dateMap.has(dateKey)) {
      dateMap.set(dateKey, new Date(battle.createdAt));
    }
  });

  const allDates = Array.from(dateMap.keys()).sort((a, b) => {
    // 날짜 내림차순 정렬 (최신순)
    return dateMap.get(b) - dateMap.get(a);
  });

  /* -----------------------------
     날짜 범위 계산 함수
  ----------------------------- */
  const getDateRange = (rangeType) => {
    const now = new Date();
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    let startDate, endDate;

    switch (rangeType) {
      case "this_week":
        const dayOfWeek = now.getDay();
        startDate = new Date(today);
        startDate.setDate(today.getDate() - dayOfWeek);
        endDate = new Date(startDate);
        endDate.setDate(startDate.getDate() + 6);
        break;
      case "last_week":
        const lastWeekDay = now.getDay();
        startDate = new Date(today);
        startDate.setDate(today.getDate() - lastWeekDay - 7);
        endDate = new Date(startDate);
        endDate.setDate(startDate.getDate() + 6);
        break;
      case "last_month":
        startDate = new Date(now.getFullYear(), now.getMonth() - 1, 1);
        endDate = new Date(now.getFullYear(), now.getMonth(), 0);
        break;
      case "last_year":
        startDate = new Date(now.getFullYear() - 1, 0, 1);
        endDate = new Date(now.getFullYear() - 1, 11, 31);
        break;
      default:
        return null;
    }

    return { startDate, endDate };
  };

  /* -----------------------------
     필터링된 배틀 목록
  ----------------------------- */
  let filteredBattles = data.battles;

  // 승패 필터 적용
  if (filter) {
    filteredBattles = filteredBattles.filter((b) => b.result === filter);
  }

  // 날짜 필터 적용
  if (dateFilter) {
    if (dateFilter.startsWith("range_")) {
      // 상대적 날짜 범위
      const rangeType = dateFilter.replace("range_", "");
      const range = getDateRange(rangeType);
      if (range) {
        filteredBattles = filteredBattles.filter((b) => {
          const battleDate = new Date(b.createdAt);
          battleDate.setHours(0, 0, 0, 0);
          return battleDate >= range.startDate && battleDate <= range.endDate;
        });
      }
    } else {
      // 특정 날짜
      filteredBattles = filteredBattles.filter(
        (b) => getDateKey(b.createdAt) === dateFilter
      );
    }
  }

  /* -----------------------------
     페이지네이션 계산
  ----------------------------- */
  const totalPages = Math.ceil(filteredBattles.length / itemsPerPage);
  const startIndex = (currentPage - 1) * itemsPerPage;
  const endIndex = startIndex + itemsPerPage;
  const paginatedBattles = filteredBattles.slice(startIndex, endIndex);

  const groupedBattles = groupBattlesByDate(paginatedBattles);

  /* -----------------------------
     필터 변경 핸들러
  ----------------------------- */
  const handleFilterClick = (filterType) => {
    if (filter === filterType) {
      setFilter(null); // 같은 필터 클릭 시 해제
    } else {
      setFilter(filterType);
    }
    setCurrentPage(1); // 필터 변경 시 첫 페이지로
  };

  const handleDateFilterClick = (date) => {
    if (dateFilter === date) {
      setDateFilter(null); // 같은 날짜 클릭 시 해제
    } else {
      setDateFilter(date);
    }
    setCurrentPage(1); // 필터 변경 시 첫 페이지로
    setIsDateFilterOpen(false); // 선택 후 드롭다운 닫기
  };

  const getDateFilterLabel = () => {
    if (!dateFilter) return "전체";
    if (dateFilter.startsWith("range_")) {
      const rangeLabels = {
        this_week: "이번 주",
        last_week: "지난 주",
        last_month: "지난 달",
        last_year: "지난 해",
      };
      return rangeLabels[dateFilter.replace("range_", "")] || dateFilter;
    }
    return dateFilter;
  };

  /* -----------------------------
     페이지 변경 핸들러
  ----------------------------- */
  const handlePageChange = (page) => {
    setCurrentPage(page);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  /* -----------------------------
     상세 페이지 이동
  ----------------------------- */
  const goToDetail = (battleId, result) => {
    navigate(`/mypage/battles/${battleId}`);
  };

  /* -----------------------------
     재대결 기능
  ----------------------------- */
  const handleResumeBattle = async (e, battle, buttonElement) => {
    e.stopPropagation(); // 카드 클릭 이벤트 방지

    try {
      // 재대결 API 호출 (GET 요청)
      const resumeRes = await axiosInstance.get(
        `/battle/${battle.battleId}/resume`
      );

      // 재대결 성공 시 BattleModal 열기
      setResumeBattleData(resumeRes.data);
      setResumeBattleId(battle.battleId);
      setShowResumeModal(true);

      // 모달이 상단에 표시되도록 스크롤을 맨 위로 이동
      window.scrollTo({
        top: 0,
        behavior: "smooth",
      });
    } catch (err) {
      console.error("재대결 실패:", err);
      alert("재대결에 실패했습니다. 다시 시도해주세요.");
    }
  };

  return (
    <div className="bh-wrapper">
      {/* 헤더 */}
      <div className="bh-header">
        <button className="bh-back" onClick={() => navigate(-1)}>
          ← 배틀 전적 기록
        </button>
      </div>

      {/* =======================
          요약 (진행중 추가)
      ======================= */}
      <div className="bh-summary">
        <div
          className={`bh-box ${filter === null ? "active" : ""}`}
          onClick={() => handleFilterClick(null)}
          style={{ cursor: "pointer" }}
        >
          <span className="val">{statistics.totalBattles}</span>
          <span className="label">총 배틀</span>
        </div>

        <div
          className={`bh-box win ${filter === "WIN" ? "active" : ""}`}
          onClick={() => handleFilterClick("WIN")}
          style={{ cursor: "pointer" }}
        >
          <span className="val">{statistics.winCount}</span>
          <span className="label">승</span>
        </div>

        <div
          className={`bh-box draw ${filter === "DRAW" ? "active" : ""}`}
          onClick={() => handleFilterClick("DRAW")}
          style={{ cursor: "pointer" }}
        >
          <span className="val">{statistics.drawCount}</span>
          <span className="label">무</span>
        </div>

        <div
          className={`bh-box lose ${filter === "LOSE" ? "active" : ""}`}
          onClick={() => handleFilterClick("LOSE")}
          style={{ cursor: "pointer" }}
        >
          <span className="val">{statistics.loseCount}</span>
          <span className="label">패</span>
        </div>

        {/* ⭐ 진행 중 */}
        <div
          className={`bh-box progress ${
            filter === "IN_PROGRESS" ? "active" : ""
          }`}
          onClick={() => handleFilterClick("IN_PROGRESS")}
          style={{ cursor: "pointer" }}
        >
          <span className="val">{statistics.inProgressCount}</span>
          <span className="label">진행 중</span>
        </div>
      </div>

      {/* =======================
          날짜 필터
      ======================= */}
      <div className="bh-date-filter">
        <div
          className="bh-date-filter-header"
          onClick={() => setIsDateFilterOpen(!isDateFilterOpen)}
        >
          <div className="bh-date-filter-label">
            <span>날짜</span>
            {dateFilter && (
              <span className="bh-date-filter-chip">
                {getDateFilterLabel()}
              </span>
            )}
          </div>
          {isDateFilterOpen ? (
            <ChevronUp size={20} />
          ) : (
            <ChevronDown size={20} />
          )}
        </div>
        {isDateFilterOpen && (
          <div className="bh-date-filter-dropdown">
            <div className="bh-date-filter-section">
              <div className="bh-date-filter-section-title">상대 날짜</div>
              <div className="bh-date-filter-options">
                <button
                  className={`bh-date-filter-option ${
                    dateFilter === null ? "active" : ""
                  }`}
                  onClick={() => handleDateFilterClick(null)}
                >
                  <span>전체</span>
                  {dateFilter === null && <span className="check">✓</span>}
                </button>
                <button
                  className={`bh-date-filter-option ${
                    dateFilter === "range_this_week" ? "active" : ""
                  }`}
                  onClick={() => handleDateFilterClick("range_this_week")}
                >
                  <span>이번 주</span>
                  {dateFilter === "range_this_week" && (
                    <span className="check">✓</span>
                  )}
                </button>
                <button
                  className={`bh-date-filter-option ${
                    dateFilter === "range_last_week" ? "active" : ""
                  }`}
                  onClick={() => handleDateFilterClick("range_last_week")}
                >
                  <span>지난 주</span>
                  {dateFilter === "range_last_week" && (
                    <span className="check">✓</span>
                  )}
                </button>
                <button
                  className={`bh-date-filter-option ${
                    dateFilter === "range_last_month" ? "active" : ""
                  }`}
                  onClick={() => handleDateFilterClick("range_last_month")}
                >
                  <span>지난 달</span>
                  {dateFilter === "range_last_month" && (
                    <span className="check">✓</span>
                  )}
                </button>
                <button
                  className={`bh-date-filter-option ${
                    dateFilter === "range_last_year" ? "active" : ""
                  }`}
                  onClick={() => handleDateFilterClick("range_last_year")}
                >
                  <span>지난 해</span>
                  {dateFilter === "range_last_year" && (
                    <span className="check">✓</span>
                  )}
                </button>
              </div>
            </div>
            {allDates.length > 0 && (
              <div className="bh-date-filter-section">
                <div className="bh-date-filter-section-title">특정 날짜</div>
                <div className="bh-date-filter-options">
                  {allDates.map((date) => (
                    <button
                      key={date}
                      className={`bh-date-filter-option ${
                        dateFilter === date ? "active" : ""
                      }`}
                      onClick={() => handleDateFilterClick(date)}
                    >
                      <span>{date}</span>
                      {dateFilter === date && <span className="check">✓</span>}
                    </button>
                  ))}
                </div>
              </div>
            )}
            {dateFilter && (
              <div className="bh-date-filter-clear">
                <button
                  className="bh-date-filter-clear-btn"
                  onClick={() => handleDateFilterClick(null)}
                >
                  필터 초기화
                </button>
              </div>
            )}
          </div>
        )}
      </div>

      {/* =======================
          리스트
      ======================= */}
      <div className="bh-list">
        {Object.keys(groupedBattles).length === 0 ? (
          <div className="bh-loading">
            {filter || dateFilter
              ? `해당 조건의 배틀 기록이 없습니다.`
              : "배틀 기록이 없습니다."}
          </div>
        ) : (
          Object.entries(groupedBattles).map(([dateKey, battles]) => (
            <div key={dateKey} className="bh-date-group">
              <div className="bh-date-header">
                <span className="bh-date-label">{dateKey}</span>
              </div>

              <div className="bh-date-battles">
                {battles.map((b) => {
                  const resultClass = b.result.toLowerCase();
                  const isWin = b.result === "WIN";
                  const isLose = b.result === "LOSE";
                  const isDraw = b.result === "DRAW";

                  return (
                    <div
                      key={b.battleId}
                      className={`bh-match-card ${resultClass}`}
                      onClick={() => goToDetail(b.battleId, b.result)}
                    >
                      {/* 상단 */}
                      <div className="bh-match-header">
                        <div className="bh-match-mode">
                          <Trophy size={16} />
                          <span>배틀 모드</span>
                        </div>
                        <div className="bh-match-meta">
                          <span className="bh-match-time">
                            {getRelativeTime(b.createdAt)}
                          </span>
                        </div>
                      </div>

                      {/* 메인 */}
                      <div className="bh-match-content">
                        {/* 좌측 */}
                        <div className="bh-match-left">
                          <div className="bh-level-badge">
                            <div className="bh-level-icon">LV</div>
                            <div className="bh-level-number">{b.level}</div>
                          </div>

                          <div className={`bh-result-badge ${resultClass}`}>
                            {isWin && "승리"}
                            {isLose && "패배"}
                            {isDraw && "무승부"}
                            {b.result === "IN_PROGRESS" && "진행 중"}
                          </div>

                          {/* 재대결 버튼 - 진행 중인 배틀만 표시 */}
                          {b.result === "IN_PROGRESS" && (
                            <button
                              className="bh-resume-btn"
                              onClick={(e) =>
                                handleResumeBattle(e, b, e.currentTarget)
                              }
                              title="재대결"
                            >
                              <RotateCcw size={16} />
                              재대결
                            </button>
                          )}
                        </div>

                        {/* 중앙 */}
                        <div className="bh-match-stats">
                          <div className="bh-stat-item">
                            <Target size={18} />
                            <div className="bh-stat-content">
                              <span className="bh-stat-value">
                                {b.correctCount} / {b.totalQuestions}
                              </span>
                              <span className="bh-stat-label">정답</span>
                            </div>
                          </div>

                          <div className="bh-stat-item">
                            <Zap size={18} />
                            <div className="bh-stat-content">
                              <span className="bh-stat-value">
                                {b.correctRate}%
                              </span>
                              <span className="bh-stat-label">정답률</span>
                            </div>
                          </div>

                          <div className="bh-stat-item">
                            <Trophy size={18} />
                            <div className="bh-stat-content">
                              <span className="bh-stat-value">
                                {b.averageScore}
                              </span>
                              <span className="bh-stat-label">평균 점수</span>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          ))
        )}
      </div>

      {/* =======================
          페이지네이션
      ======================= */}
      {totalPages > 1 && (
        <div className="bh-pagination">
          <button
            className="bh-pagination-btn"
            onClick={() => handlePageChange(currentPage - 1)}
            disabled={currentPage === 1}
          >
            이전
          </button>

          <div className="bh-pagination-numbers">
            {Array.from({ length: totalPages }, (_, i) => i + 1).map((page) => {
              // 현재 페이지 주변 2페이지만 표시
              if (
                page === 1 ||
                page === totalPages ||
                (page >= currentPage - 2 && page <= currentPage + 2)
              ) {
                return (
                  <button
                    key={page}
                    className={`bh-pagination-number ${
                      currentPage === page ? "active" : ""
                    }`}
                    onClick={() => handlePageChange(page)}
                  >
                    {page}
                  </button>
                );
              } else if (page === currentPage - 3 || page === currentPage + 3) {
                return (
                  <span key={page} className="bh-pagination-ellipsis">
                    ...
                  </span>
                );
              }
              return null;
            })}
          </div>

          <button
            className="bh-pagination-btn"
            onClick={() => handlePageChange(currentPage + 1)}
            disabled={currentPage === totalPages}
          >
            다음
          </button>
        </div>
      )}

      {/* 재대결 모달 */}
      {showResumeModal && resumeBattleData && (
        <BattleModal
          id={resumeBattleData.sourceId || resumeBattleId}
          onClose={() => {
            setShowResumeModal(false);
            setResumeBattleData(null);
            setResumeBattleId(null);
          }}
          sourceType={resumeBattleData.sourceType || "CHAT"}
          initialBattle={resumeBattleData}
          position="top"
        />
      )}
    </div>
  );
}
