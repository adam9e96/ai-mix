import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import axiosInstance from "../../api/axiosInstance";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import { RotateCcw } from "lucide-react";
import BattleModal from "../../components/battle/BattleModal";
import "@styles/pages/battle-detail.css";

export default function BattleDetail() {
  const { battleId } = useParams();
  const navigate = useNavigate();
  const [data, setData] = useState(null);
  const [showResumeModal, setShowResumeModal] = useState(false);
  const [resumeBattleData, setResumeBattleData] = useState(null);

  useEffect(() => {
    axiosInstance
      .get(`/battle/${battleId}/result`)
      .then((res) => setData(res.data))
      .catch(() => navigate(-1));
  }, [battleId, navigate]);

  /* -----------------------------
     재대결 기능
  ----------------------------- */
  const handleResumeBattle = async () => {
    try {
      // 재대결 API 호출 (GET 요청)
      const resumeRes = await axiosInstance.get(`/battle/${battleId}/resume`);

      // 재대결 성공 시 BattleModal 열기
      setResumeBattleData(resumeRes.data);
      setShowResumeModal(true);

      // 모달이 화면 중앙에 표시되도록 스크롤 조정
      setTimeout(() => {
        window.scrollTo({
          top: 0,
          behavior: "smooth",
        });
      }, 100);
    } catch (err) {
      console.error("재대결 실패:", err);
      alert("재대결에 실패했습니다. 다시 시도해주세요.");
    }
  };

  if (!data) return <div className="loading">불러오는 중...</div>;

  const {
    result, // ⭐ 추가
    statistics,
    questionResults,
    evaluation,
  } = data;

  // 결과 텍스트 매핑
  const resultTextMap = {
    WIN: "승리",
    LOSE: "패배",
    DRAW: "무승부",
    IN_PROGRESS: "진행 중",
  };

  return (
    <div className="battle-detail">
      {/* 헤더 */}
      <div className="bd-header">
        <button
          className="bd-back"
          onClick={() => navigate("/mypage/battle-history")}
        >
          ← 목록으로 돌아가기
        </button>
      </div>

      {/* 상단 요약 */}
      <section className="battle-summary">
        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            marginBottom: "16px",
          }}
        >
          {/* ⭐ 결과 표시 (기존 구조에 최소 추가) */}
          <div className={`battle-result-badge ${result?.toLowerCase()}`}>
            {resultTextMap[result]}
          </div>

          {/* 재도전 버튼 - 진행 중인 배틀만 표시 */}
          {result === "IN_PROGRESS" && (
            <button
              className="bd-resume-btn"
              onClick={handleResumeBattle}
              title="재대결"
            >
              <RotateCcw size={18} />
              재대결
            </button>
          )}
        </div>

        <div className="summary-grid">
          <div>
            총 점수: <b>{statistics.totalScore}</b>
          </div>
          <div>평균 점수: {statistics.averageScore}</div>
          <div>정답률: {statistics.correctRate}%</div>
          <div>문항 수: {statistics.totalQuestions}</div>
        </div>
      </section>

      {/* 문제별 결과 */}
      <section className="battle-questions">
        {questionResults.map((q) => (
          <div
            key={q.questionId}
            className={`question-card ${q.isCorrect ? "correct" : "wrong"}`}
          >
            <div className="question-header">
              <span className="order">Q{q.orderNo}</span>
              <span className="difficulty">{q.difficulty}</span>
            </div>

            <div className="question-text">{q.questionText}</div>

            {/* 객관식 문제일 때만 보기 출력 */}
            {q.questionType === "OBJECTIVE" && Array.isArray(q.choices) && (
              <ul className="choices">
                {q.choices.map((choice, idx) => {
                  const no = String(idx + 1);
                  const isUser = q.userAnswer === no;
                  const isAnswer = q.correctAnswer === no;

                  return (
                    <li
                      key={idx}
                      className={[
                        isAnswer && "answer",
                        isUser && !isAnswer && "user-wrong",
                        isUser && isAnswer && "user-correct",
                      ]
                        .filter(Boolean)
                        .join(" ")}
                    >
                      {no}. {choice}
                    </li>
                  );
                })}
              </ul>
            )}

            <div className="feedback">
              <ReactMarkdown remarkPlugins={[remarkGfm]}>
                {q.feedback}
              </ReactMarkdown>
            </div>
          </div>
        ))}
      </section>

      {/* AI 평가 */}
      <section className="battle-evaluation">
        <h3>AI 종합 평가</h3>
        <div className="grade">등급: {evaluation.grade}</div>
        <p>{evaluation.gradeDescription}</p>

        <ul>
          <li>
            <b>강점</b>: {evaluation.strengths}
          </li>
          <li>
            <b>약점</b>: {evaluation.weaknesses}
          </li>
          <li>
            <b>추천</b>: {evaluation.recommendation}
          </li>
        </ul>
      </section>

      {/* 재대결 모달 */}
      {showResumeModal && resumeBattleData && (
        <BattleModal
          id={resumeBattleData.sourceId || battleId}
          onClose={() => {
            setShowResumeModal(false);
            setResumeBattleData(null);
            // 모달 닫은 후 데이터 새로고침
            axiosInstance
              .get(`/battle/${battleId}/result`)
              .then((res) => setData(res.data))
              .catch(() => navigate(-1));
          }}
          sourceType={resumeBattleData.sourceType || "CHAT"}
          initialBattle={resumeBattleData}
          position="top"
        />
      )}
    </div>
  );
}
