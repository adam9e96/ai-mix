import { useEffect, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { motion, AnimatePresence } from "framer-motion";
import "@styles/pages/qnalist.css";
import { useQnaStore } from "@/stores/qna.list.store";
import { useAuthStore } from "@/stores/auth.store";
import { useUIStore } from "@/stores/ui.store";
import { Search, X, Network } from "lucide-react";
import { IoInformationCircleSharp } from "react-icons/io5";
import { Tooltip } from "react-tooltip";
import TagGraph from "@/components/qna/TagGraph";
import LoginModal from "@/components/modal/LoginModal";

export default function QnaList() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const [localSearchKeyword, setLocalSearchKeyword] = useState("");
  const [showTagGraph, setShowTagGraph] = useState(false);
  const { isAuthenticated } = useAuthStore();
  const { showLoginModal, setShowLoginModal } = useUIStore();

  const {
    list,
    pageInfo,
    loadQnaList,
    loading,
    searchType,
    searchKeyword,
    selectedTags,
    setSearchType,
    setSearchKeyword,
    setSelectedTags,
    addSelectedTag,
    removeSelectedTag,
  } = useQnaStore();

  // URL 쿼리 파라미터에서 초기값 읽기
  useEffect(() => {
    const urlPage = parseInt(searchParams.get("page") || "0", 10);
    const urlSize = parseInt(searchParams.get("size") || "5", 10);
    const urlSearchType = searchParams.get("searchType") || "title";
    const urlKeyword = searchParams.get("keyword") || "";
    const urlTags = searchParams.getAll("tag") || [];

    // URL 파라미터가 있으면 스토어와 동기화
    if (urlKeyword) {
      setSearchKeyword(urlKeyword);
      setLocalSearchKeyword(urlKeyword);
    }
    if (urlSearchType !== searchType) {
      setSearchType(urlSearchType);
    }
    if (urlTags.length > 0) {
      setSelectedTags(urlTags);
    }

    // URL 파라미터로 목록 로드
    loadQnaList(urlPage, urlSearchType, urlKeyword);
    // 화면 전환 시 스크롤을 상단으로 이동
    window.scrollTo(0, 0);
  }, []); // 초기 마운트 시에만 실행

  // 페이지네이션/검색 상태 변경 시 URL 업데이트
  useEffect(() => {
    const params = new URLSearchParams();
    params.set("page", pageInfo.page.toString());
    params.set("size", pageInfo.size.toString());

    if (searchKeyword && searchKeyword.trim()) {
      params.set("searchType", searchType);
      params.set("keyword", searchKeyword.trim());
    }

    if (selectedTags && selectedTags.length > 0) {
      selectedTags.forEach((tag) => {
        if (tag && tag.trim()) {
          params.append("tag", tag.trim());
        }
      });
    }

    // URL 업데이트 (히스토리 추가하지 않음)
    setSearchParams(params, { replace: true });
  }, [
    pageInfo.page,
    pageInfo.size,
    searchType,
    searchKeyword,
    selectedTags,
    setSearchParams,
  ]);

  // 검색어 동기화
  useEffect(() => {
    setLocalSearchKeyword(searchKeyword);
  }, [searchKeyword]);

  const handleSearch = () => {
    const trimmedKeyword = localSearchKeyword.trim();
    setSearchKeyword(trimmedKeyword);
    setSelectedTags([]); // 검색 시 태그 필터 초기화
    // 검색어를 직접 전달하여 즉시 검색 실행 (첫 페이지로 리셋)
    loadQnaList(0, searchType, trimmedKeyword);
  };

  const handleTagClick = (tag, e) => {
    e?.preventDefault();
    e?.stopPropagation();
    if (!selectedTags.includes(tag)) {
      addSelectedTag(tag);
      setSearchKeyword(""); // 태그 필터링 시 검색어 초기화
      setLocalSearchKeyword("");
      // 태그 추가 후 목록 다시 로드 (loadQnaList 내부에서 get().selectedTags로 최신 상태 가져옴)
      loadQnaList(0, searchType, "");
    }
  };

  const handleTagRemove = (tag) => {
    removeSelectedTag(tag);
    // 태그 제거 후 목록 다시 로드 (loadQnaList 내부에서 get().selectedTags로 최신 상태 가져옴)
    loadQnaList(0, searchType, searchKeyword);
  };

  const handleKeyPress = (e) => {
    if (e.key === "Enter") {
      handleSearch();
    }
  };

  const handleSearchTypeChange = (e) => {
    const newSearchType = e.target.value;
    setSearchType(newSearchType);
    // 검색 타입만 변경하고 자동 검색은 하지 않음
  };

  const handleAskClick = () => {
    // ✅ 로그인 여부 상관없이 질문 가능
    navigate("/qna/ask");
  };

  return (
    <div className="qna-list-wrapper">
      <div className="qna-container">
        <div className="qna-header">
          <div className="qna-title-wrapper">
            <Link
              to="/qna"
              className="qna-title-link"
              onClick={(e) => {
                if (window.location.pathname === "/qna") {
                  e.preventDefault();
                  window.location.reload();
                }
              }}
            >
              <h2 className="qna-title">Q&A 목록</h2>
            </Link>
            <button
              className="qna-info-icon"
              data-tooltip-id="qna-info-tooltip"
              data-tooltip-html="질문을 올리고 커뮤니티와 AI의 답변을 받아보세요.<br />익명으로 질문할 수도 있으며, 답변에 추천/비추천을 할 수 있습니다."
            >
              <IoInformationCircleSharp size={20} />
            </button>
            <Tooltip
              id="qna-info-tooltip"
              place="bottom"
              className="qna-info-tooltip"
            />
          </div>
          <div style={{ display: "flex", gap: "10px" }}>
            <button
              className="qna-tag-graph-button"
              onClick={() => {
                if (!isAuthenticated()) {
                  setShowLoginModal(true);
                  return;
                }
                setShowTagGraph(true);
              }}
              title="태그 관계 시각화"
            >
              <Network size={18} />
              <span>게시물 시각화</span>
            </button>
            <button className="qna-ask-button" onClick={handleAskClick}>
              질문하기
            </button>
          </div>
        </div>

        {/* 태그 필터 표시 */}
        {selectedTags && selectedTags.length > 0 && (
          <div
            className="qna-tag-filter"
            style={{
              marginBottom: "20px",
              padding: "12px",
              background: "#f3f4f6",
              borderRadius: "8px",
              display: "flex",
              alignItems: "center",
              gap: "10px",
              flexWrap: "wrap",
            }}
          >
            <span style={{ color: "#6b7280", fontSize: "14px" }}>
              태그 필터:
            </span>
            {selectedTags.map((tag, index) => (
              <span
                key={index}
                className="qna-tag"
                style={{
                  background: "#6c63ff",
                  color: "white",
                  padding: "4px 12px 4px 12px",
                  borderRadius: "16px",
                  fontSize: "13px",
                  display: "inline-flex",
                  alignItems: "center",
                  gap: "6px",
                }}
              >
                {tag}
                <button
                  onClick={(e) => {
                    e.preventDefault();
                    e.stopPropagation();
                    handleTagRemove(tag);
                  }}
                  style={{
                    background: "transparent",
                    border: "none",
                    color: "white",
                    cursor: "pointer",
                    padding: "0",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    width: "16px",
                    height: "16px",
                    borderRadius: "50%",
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.background =
                      "rgba(255, 255, 255, 0.2)";
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.background = "transparent";
                  }}
                >
                  <X size={12} />
                </button>
              </span>
            ))}
          </div>
        )}

        <motion.div
          className="qna-list"
          initial="hidden"
          animate="visible"
          variants={{
            hidden: { opacity: 0 },
            visible: {
              opacity: 1,
              transition: {
                staggerChildren: 0.05,
              },
            },
          }}
        >
          {loading && <p className="qna-loading">불러오는 중...</p>}

          {!loading &&
            list.map((q) => (
              <motion.div
                key={q.id}
                variants={{
                  hidden: { opacity: 0, y: 20 },
                  visible: { opacity: 1, y: 0 },
                }}
              >
                <Link to={`/qna/question/${q.id}`} className="qna-card">
                  {/* 왼쪽: 통계 */}
                  <div className="qna-card-stats">
                    <div
                      className={`qna-answer-box ${
                        q.answerCount > 0 ? "has-answers" : ""
                      }`}
                    >
                      {q.answerCount > 0 && (
                        <span className="qna-check-icon">✓</span>
                      )}
                      <span className="qna-answer-count">
                        {q.answerCount || 0}
                      </span>
                      <span className="qna-answer-label">답변</span>
                    </div>
                    <div className="qna-view-stat">
                      <span className="qna-view-number">
                        {q.viewCount >= 1000
                          ? `${(q.viewCount / 1000).toFixed(1)}k`
                          : q.viewCount || 0}
                      </span>
                      <span className="qna-view-label">조회</span>
                    </div>
                  </div>

                  {/* 오른쪽: 내용 */}
                  <div className="qna-card-content">
                    <h3 className="qna-card-title">{q.title}</h3>
                    <p className="qna-card-preview">{q.bodyPreview}</p>

                    {/* 태그 */}
                    {q.tags && q.tags.length > 0 && (
                      <div className="qna-card-tags">
                        {q.tags.map((tag, index) => (
                          <span
                            key={index}
                            className="qna-tag clickable"
                            onClick={(e) => handleTagClick(tag, e)}
                            style={{ cursor: "pointer" }}
                          >
                            {tag}
                          </span>
                        ))}
                      </div>
                    )}

                    {/* 작성자/날짜 */}
                    <div className="qna-card-meta">
                      <span className="qna-author">
                        {q.isAnonymous ? "익명" : q.authorNickname || "익명"}
                      </span>
                      <span className="qna-date">
                        {q.createdAt
                          ? new Date(q.createdAt).toLocaleDateString("ko-KR", {
                              year: "numeric",
                              month: "long",
                              day: "numeric",
                            })
                          : ""}
                      </span>
                    </div>
                  </div>
                </Link>
              </motion.div>
            ))}
        </motion.div>

        <div className="qna-pagination">
          {/* 첫 페이지 버튼 */}
          <button
            className="qna-pagination-btn"
            disabled={pageInfo.page === 0}
            onClick={() => loadQnaList(0, searchType, searchKeyword)}
            aria-label="첫 페이지"
          >
            «
          </button>

          {/* 이전 페이지 버튼 */}
          <button
            className="qna-pagination-btn"
            disabled={pageInfo.page === 0}
            onClick={() =>
              loadQnaList(pageInfo.page - 1, searchType, searchKeyword)
            }
            aria-label="이전 페이지"
          >
            ‹
          </button>

          {/* 페이지 번호 버튼들 */}
          {(() => {
            const totalPages = pageInfo.totalPages;
            const currentPage = pageInfo.page;
            const maxVisible = 10;

            let startPage, endPage;

            if (totalPages <= maxVisible) {
              // 전체 페이지가 10개 이하면 모두 표시
              startPage = 0;
              endPage = totalPages - 1;
            } else {
              // 전체 페이지가 10개 초과면 현재 페이지 기준으로 표시
              startPage = Math.max(0, currentPage - 4);
              endPage = Math.min(totalPages - 1, startPage + maxVisible - 1);

              // 끝에서 시작점 조정
              if (endPage - startPage < maxVisible - 1) {
                startPage = Math.max(0, endPage - maxVisible + 1);
              }
            }

            const pages = [];
            for (let i = startPage; i <= endPage; i++) {
              pages.push(i);
            }

            return pages.map((pageNum) => (
              <button
                key={pageNum}
                className={`qna-pagination-btn ${
                  pageNum === currentPage ? "active" : ""
                }`}
                onClick={() => loadQnaList(pageNum, searchType, searchKeyword)}
              >
                {pageNum + 1}
              </button>
            ));
          })()}

          {/* 다음 페이지 버튼 */}
          <button
            className="qna-pagination-btn"
            disabled={pageInfo.page + 1 >= pageInfo.totalPages}
            onClick={() =>
              loadQnaList(pageInfo.page + 1, searchType, searchKeyword)
            }
            aria-label="다음 페이지"
          >
            ›
          </button>

          {/* 마지막 페이지 버튼 */}
          <button
            className="qna-pagination-btn"
            disabled={pageInfo.page + 1 >= pageInfo.totalPages}
            onClick={() =>
              loadQnaList(pageInfo.totalPages - 1, searchType, searchKeyword)
            }
            aria-label="마지막 페이지"
          >
            »
          </button>
        </div>

        {/* 검색창 */}
        <div className="qna-search-wrapper">
          <select
            className="qna-search-select"
            value={searchType}
            onChange={handleSearchTypeChange}
          >
            <option value="title">제목</option>
            <option value="author">작성자</option>
            <option value="content">내용</option>
          </select>
          <input
            type="text"
            className="qna-search-input"
            placeholder={
              searchType === "title"
                ? "제목으로 검색..."
                : searchType === "author"
                ? "작성자로 검색..."
                : "내용으로 검색..."
            }
            value={localSearchKeyword}
            onChange={(e) => setLocalSearchKeyword(e.target.value)}
            onKeyPress={handleKeyPress}
          />
          <button className="qna-search-button" onClick={handleSearch}>
            <Search size={18} />
          </button>
        </div>
      </div>

      {/* 태그 그래프 모달 */}
      {showTagGraph && <TagGraph onClose={() => setShowTagGraph(false)} />}

      {/* 로그인 모달 */}
      {showLoginModal && (
        <LoginModal
          onClose={() => setShowLoginModal(false)}
          onSuccess={() => {
            // 로그인 성공 후 필요한 작업 수행
          }}
        />
      )}
    </div>
  );
}
