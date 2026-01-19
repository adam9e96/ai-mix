import { X } from "lucide-react";
import { useState, useEffect, useRef } from "react";
import { motion, AnimatePresence } from "framer-motion";
import axiosInstance from "../../api/axiosInstance";
import "../../styles/components/battleModal.css";
import { useBattleStore } from "../../stores/battle.store";
import { GiRuleBook } from "react-icons/gi";

export default function BattleModal({
  id,
  onClose,
  sourceType = "CHAT",
  questionType: fixedQuestionType = null,
  initialBattle = null, // 재대결 시 기존 배틀 정보
  position = null, // 모달 위치 (x, y 좌표)
}) {
  const { fetchBattleSummary, addBattleSummary } = useBattleStore();
  const [loading, setLoading] = useState(false);
  const [battle, setBattle] = useState(initialBattle);
  const [accepted, setAccepted] = useState(!!initialBattle);

  const [selectedType, setSelectedType] = useState(
    fixedQuestionType || initialBattle?.questions?.[0]?.questionType || null
  );
  // 재대결 시 다음 문제부터 시작
  const [currentIndex, setCurrentIndex] = useState(
    initialBattle?.nextQuestionOrder ? initialBattle.nextQuestionOrder - 1 : 0
  );

  const [userAnswer, setUserAnswer] = useState("");
  const [answerResult, setAnswerResult] = useState(null);
  const [finalResult, setFinalResult] = useState(null);
  const [showRulesModal, setShowRulesModal] = useState(false);

  /* ===========================
      객관식 정규화 (A→1)
  ============================ */
  const normalizeAnswerForBackend = (answerKey) => {
    if (!answerKey) return null;

    if (/^[A-D]$/.test(answerKey)) {
      return String(answerKey.charCodeAt(0) - 65 + 1);
    }
    if (/^[0-9]+$/.test(answerKey)) return answerKey;
    return "1";
  };

  /* ===========================
      보기 텍스트 출력용
  ============================ */
  const getChoiceText = (question, answerValue) => {
    if (!question || !answerValue) return answerValue;

    let index = -1;
    if (/^[A-D]$/.test(answerValue)) index = answerValue.charCodeAt(0) - 65;
    else if (/^[0-9]+$/.test(answerValue)) index = Number(answerValue) - 1;

    if (index < 0 || index >= question.choices.length) return answerValue;

    return `${String.fromCharCode(65 + index)}. ${question.choices[index]}`;
  };

  /* ===========================
      문제 생성
  ============================ */
  const startBattle = async () => {
    if (!selectedType) return;
    setLoading(true);

    try {
      const res = await axiosInstance.post("/battle/create", {
        id,
        questionType: selectedType,
        sourceType: sourceType,
      });

      setBattle(res.data);
      setAccepted(true);
      setCurrentIndex(0);
      setUserAnswer("");
      setAnswerResult(null);
      setFinalResult(null);
    } catch (err) {
      console.error("문제 생성 실패:", err);
    } finally {
      setLoading(false);
    }
  };

  /* ===========================
      답안 제출
  ============================ */
  const submitAnswer = async () => {
    const question = battle.questions[currentIndex];

    const transformedAnswer =
      question.questionType === "OBJECTIVE"
        ? normalizeAnswerForBackend(userAnswer)
        : userAnswer;

    try {
      const res = await axiosInstance.post("/battle/submit-answer", {
        battleId: battle.battleId,
        questionId: question.questionId,
        userAnswer: transformedAnswer,
      });

      setAnswerResult(res.data);
      // ✅ 여기서 더 이상 최종결과 자동 조회 안 함
    } catch (err) {
      console.error("답안 제출 실패:", err);
    }
  };

  /* ===========================
      최종 결과 조회 (버튼)
  ============================ */
  const loadFinalResult = async () => {
    try {
      const resultRes = await axiosInstance.get(
        `/battle/${battle.battleId}/result`
      );
      setFinalResult(resultRes.data);

      // 배틀 완료 후 summary 가져와서 스토어에 저장
      if (id && battle.battleId) {
        const summary = await fetchBattleSummary(battle.battleId);
        if (summary) {
          addBattleSummary(id, summary);
        }
      }
    } catch (err) {
      console.error("결과 조회 실패:", err);
    }
  };

  /* ===========================
   배틀 재도전
=========================== */
  const retryBattle = async () => {
    if (!battle?.battleId) return;

    setLoading(true);

    try {
      const res = await axiosInstance.post(`/battle/${battle.battleId}/retry`);

      // ✅ 서버에서 새로 초기화된 battle 내려준다고 가정
      setBattle(res.data);

      // 상태 전부 초기화 (중요)
      setCurrentIndex(0);
      setUserAnswer("");
      setAnswerResult(null);
      setFinalResult(null);
      setAccepted(true);
    } catch (err) {
      console.error("배틀 재도전 실패:", err);
    } finally {
      setLoading(false);
    }
  };

  /* ===========================
      다음 문제
  ============================ */
  const goNext = () => {
    setCurrentIndex((prev) => prev + 1);
    setUserAnswer("");
    setAnswerResult(null);
  };

  const currentQuestion = battle?.questions?.[currentIndex];
  const backdropRef = useRef(null);

  // 모달이 열릴 때 backdrop 스크롤을 맨 위로 이동 (모달이 화면 중앙에 오도록)
  useEffect(() => {
    if (backdropRef.current) {
      backdropRef.current.scrollTop = 0;
    }
  }, []);

  // 모달 위치 계산 (클릭한 영역 근처에 표시)
  const isTopPosition = position === "top";
  const isPositioned = position && position !== "top";

  const backdropStyle = isTopPosition
    ? {
        display: "flex",
        justifyContent: "center",
        alignItems: "flex-start",
        paddingTop: "20px",
      }
    : isPositioned
    ? {
        display: "block",
        justifyContent: "flex-start",
        alignItems: "flex-start",
      }
    : {};

  const modalStyle = isTopPosition
    ? {
        position: "relative",
        margin: "0 auto",
        transform: "none",
      }
    : isPositioned
    ? {
        position: "absolute",
        left: `${Math.max(
          20,
          Math.min(position.x - 350, window.innerWidth - 720)
        )}px`,
        top: `${Math.max(
          20,
          Math.min(position.y - 100, window.innerHeight - 500)
        )}px`,
        transform: "none",
      }
    : {};

  return (
    <motion.div
      ref={backdropRef}
      className={`battle-backdrop ${
        isTopPosition ? "battle-backdrop-top" : ""
      }`}
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      onClick={onClose}
      style={backdropStyle}
    >
      <motion.div
        className="battle-box battle-animate"
        initial={{ opacity: 0, scale: 0.9, y: 20 }}
        animate={{ opacity: 1, scale: 1, y: 0 }}
        exit={{ opacity: 0, scale: 0.9, y: 20 }}
        transition={{ type: "spring", damping: 25 }}
        onClick={(e) => e.stopPropagation()}
        // @ts-ignore - framer-motion style prop type compatibility
        style={modalStyle || undefined}
      >
        <button className="battle-close mac-close" onClick={onClose}>
          <X size={15} />
        </button>

        <div className="battle-header-top">
          <div className="battle-header-title-group">
            <h2>⚔ AI 배틀 모드</h2>
            {initialBattle && (
              <span className="battle-resume-header-badge">재대결</span>
            )}
            {finalResult && (
              <button className="battle-retry-btn-header" onClick={retryBattle}>
                🔁 재도전
              </button>
            )}
          </div>
        </div>

        {/* ------------------------------
           최종 결과 화면
        ------------------------------ */}
        {finalResult && (
          <div className="battle-final">
            {/* ===== 상단 중앙: 배틀 결과 + 승패 ===== */}
            <div className="battle-result-header center">
              <span className="result-icon">
                {finalResult.result === "WIN" && "🏆"}
                {finalResult.result === "LOSE" && "💀"}
                {finalResult.result === "DRAW" && "🤝"}
              </span>

              <h2 className="result-title">배틀 결과</h2>

              <span
                className={`result-text ${finalResult.result?.toLowerCase()}`}
              >
                {finalResult.result === "WIN" && "승리"}
                {finalResult.result === "LOSE" && "패배"}
                {finalResult.result === "DRAW" && "무승부"}
              </span>
            </div>

            {/* ===== 결과 박스 (통계 갤러리 + 등급) ===== */}
            <div className="battle-result-container">
              {/* 통계 갤러리 */}
              <div className="result-stats-gallery">
                <div className="stat-item">
                  <div className="stat-value">
                    {finalResult.statistics.totalQuestions}
                  </div>
                  <div className="stat-label">총 문제</div>
                </div>
                <div className="stat-item">
                  <div className="stat-value correct">
                    {finalResult.statistics.correctCount}
                  </div>
                  <div className="stat-label">정답</div>
                </div>
                <div className="stat-item">
                  <div className="stat-value incorrect">
                    {finalResult.statistics.incorrectCount}
                  </div>
                  <div className="stat-label">오답</div>
                </div>
                <div className="stat-item">
                  <div className="stat-value rate">
                    {Math.round(finalResult.statistics.correctRate)}%
                  </div>
                  <div className="stat-label">정답률</div>
                </div>
                <div className="stat-item">
                  <div className="stat-value score">
                    {finalResult.statistics.totalScore}
                  </div>
                  <div className="stat-label">총점</div>
                </div>
                <div className="stat-item grade-item">
                  <div
                    className={`stat-value grade-value grade-${
                      finalResult.evaluation.grade?.replace(/\+/g, "") || "D"
                    }`}
                  >
                    {finalResult.evaluation.grade}
                  </div>
                  <div className="stat-label">등급</div>
                </div>
              </div>
            </div>

            {/* ===== AI 평가 ===== */}
            <div className="result-feedback">
              <h3>📘 AI 평가</h3>
              <p>{finalResult.evaluation.gradeDescription}</p>
              <p>{finalResult.evaluation.comment}</p>
              <p>{finalResult.evaluation.recommendation}</p>
            </div>

            {/* 버튼 */}
            <div className="final-actions">
              <button className="close-btn" onClick={onClose}>
                닫기
              </button>
            </div>
          </div>
        )}

        {/* ------------------------------
            문제 화면
        ------------------------------ */}
        {!finalResult && (
          <div className="battle-content">
            {!accepted && !loading && (
              <div className="battle-intro">
                <div className="battle-intro-header">
                  <h3>배틀 시작</h3>
                  {/* 배틀 규칙 버튼 */}
                  <button
                    className="battle-rules-btn"
                    onClick={() => setShowRulesModal(true)}
                  >
                    <GiRuleBook size={18} />
                    <span>배틀 규칙</span>
                  </button>
                </div>
                {fixedQuestionType ? (
                  <p>주관식 문제로 배틀을 시작합니다</p>
                ) : (
                  <p>문제 유형을 선택하세요</p>
                )}

                {!fixedQuestionType && (
                  <div className="battle-select-type">
                    <button
                      className={`type-btn ${
                        selectedType === "OBJECTIVE" ? "active" : ""
                      }`}
                      onClick={() => setSelectedType("OBJECTIVE")}
                    >
                      객관식
                    </button>
                    <button
                      className={`type-btn ${
                        selectedType === "SUBJECTIVE" ? "active" : ""
                      }`}
                      onClick={() => setSelectedType("SUBJECTIVE")}
                    >
                      주관식
                    </button>
                  </div>
                )}

                <button
                  className="battle-accept-btn"
                  disabled={!selectedType}
                  onClick={startBattle}
                >
                  문제 시작하기
                </button>
              </div>
            )}

            {loading && <div className="battle-loading">문제 생성중...</div>}

            {!loading && battle && accepted && currentQuestion && (
              <>
                {/* 배틀 진행률 표시 */}
                <div className="battle-progress-card">
                  <div className="battle-progress-header">
                    <span className="battle-progress-label">현재 진행률</span>
                  </div>
                  <div className="battle-progress-percentage">
                    {Math.round(
                      ((currentIndex + 1) / battle.totalQuestions) * 100
                    )}
                    % 진행중
                  </div>
                  <div className="battle-progress-bar-container">
                    <div
                      className="battle-progress-bar"
                      style={{
                        width: `${
                          ((currentIndex + 1) / battle.totalQuestions) * 100
                        }%`,
                      }}
                    ></div>
                    {/* 모든 문제 점들 */}
                    {Array.from({ length: battle.totalQuestions }).map(
                      (_, idx) => {
                        const isCompleted = idx <= currentIndex;
                        const position =
                          battle.totalQuestions === 1
                            ? 50
                            : (idx / (battle.totalQuestions - 1)) * 100;
                        return (
                          <div
                            key={idx}
                            className={`battle-progress-dot ${
                              isCompleted ? "completed" : "pending"
                            }`}
                            style={{
                              left: `${position}%`,
                            }}
                          />
                        );
                      }
                    )}
                  </div>
                </div>

                <div className="battle-question-box">
                  <div className="battle-question-header">
                    <h4>문제 {currentQuestion.orderNo}</h4>
                    <span className="battle-level-badge">
                      레벨 {battle.level}
                    </span>
                  </div>
                  <p className="battle-question">
                    {currentQuestion.questionText}
                  </p>
                </div>

                {/* 객관식 */}
                {currentQuestion.questionType === "OBJECTIVE" &&
                  !answerResult && (
                    <div className="objective-choice-box">
                      {currentQuestion.choices.map((c, idx) => {
                        const key = String.fromCharCode(65 + idx);
                        return (
                          <button
                            key={idx}
                            className={`choice-btn ${
                              userAnswer === key ? "selected" : ""
                            }`}
                            onClick={() => setUserAnswer(key)}
                          >
                            <div className="choice-radio">
                              {userAnswer === key && <div className="dot" />}
                            </div>
                            <span className="choice-text">
                              {key}. {c}
                            </span>
                          </button>
                        );
                      })}
                    </div>
                  )}

                {/* 주관식 */}
                {currentQuestion.questionType === "SUBJECTIVE" &&
                  !answerResult && (
                    <div className="battle-answer-input">
                      <textarea
                        placeholder="답변을 입력하세요"
                        value={userAnswer}
                        onChange={(e) => setUserAnswer(e.target.value)}
                        rows={4}
                      />
                    </div>
                  )}

                {!answerResult && (
                  <button
                    className="submit-btn"
                    disabled={!userAnswer}
                    onClick={submitAnswer}
                  >
                    제출
                  </button>
                )}

                {/* 제출 후 결과 표시 */}
                {answerResult && (
                  <div className="battle-result">
                    <div className="compare-wrapper">
                      <div className="compare-box">
                        <div className="compare-header">👤 나의 답변</div>
                        <div className="compare-content">
                          {currentQuestion.questionType === "OBJECTIVE"
                            ? getChoiceText(
                                currentQuestion,
                                answerResult.userAnswer
                              )
                            : answerResult.userAnswer}
                        </div>
                      </div>

                      <div className="compare-box">
                        <div className="compare-header">✔ 정답</div>
                        <div className="compare-content">
                          {currentQuestion.questionType === "OBJECTIVE"
                            ? getChoiceText(
                                currentQuestion,
                                answerResult.correctAnswer
                              )
                            : answerResult.correctAnswer}
                        </div>
                      </div>
                    </div>

                    <div
                      className={`score-box ${
                        answerResult.isCorrect ? "correct" : "wrong"
                      }`}
                    >
                      점수: {answerResult.score}점{" "}
                      {answerResult.isCorrect ? "✅ 정답!" : "❌ 오답"}
                    </div>

                    <div className="feedback-box">
                      <h3>AI 피드백</h3>
                      <p>{answerResult.feedback}</p>
                    </div>

                    {currentIndex < battle.totalQuestions - 1 && (
                      <button className="next-btn" onClick={goNext}>
                        다음 문제
                      </button>
                    )}

                    {currentIndex === battle.totalQuestions - 1 && (
                      <>
                        <p className="finish-text">
                          🎉 모든 문제를 완료했습니다!
                        </p>
                        {/* ✅ 버튼만 추가 (기존 클래스 유지) */}
                        <button className="next-btn" onClick={loadFinalResult}>
                          결과 확인하러 가기
                        </button>
                      </>
                    )}
                  </div>
                )}
              </>
            )}
          </div>
        )}
      </motion.div>

      {/* 배틀 규칙 모달 */}
      <AnimatePresence>
        {showRulesModal && (
          <motion.div
            className="battle-rules-modal-backdrop"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={() => setShowRulesModal(false)}
          >
            <motion.div
              className="battle-rules-modal"
              initial={{ scale: 0.9, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.9, opacity: 0 }}
              onClick={(e) => e.stopPropagation()}
            >
              <div className="battle-rules-modal-header">
                <h2>배틀 모드 규칙</h2>
                <button
                  className="battle-rules-modal-close"
                  onClick={() => setShowRulesModal(false)}
                >
                  <X size={20} />
                </button>
              </div>

              <div className="battle-rules-modal-content">
                <div className="battle-rule-section">
                  <h3>📝 문제 구성</h3>
                  <ul>
                    <li>3~5개의 문제가 출제됩니다</li>
                    <li>객관식 또는 주관식 문제로 구성됩니다</li>
                    <li>난이도는 EASY, MEDIUM, HARD로 구분됩니다</li>
                  </ul>
                </div>

                <div className="battle-rule-section">
                  <h3>✅ 정답 판정</h3>
                  <ul>
                    <li>80점 이상을 정답으로 인정합니다</li>
                    <li>
                      주관식 문제는 GPT가 채점하여 점수와 피드백을 제공합니다
                    </li>
                  </ul>
                </div>

                <div className="battle-rule-section">
                  <h3>📊 배틀 결과 해석 가이드</h3>

                  <h4>📈 통계 정보</h4>
                  <ul>
                    <li>
                      <b>평균 점수</b>: 답변한 모든 문제의 평균 점수입니다
                    </li>
                    <li>
                      <b>정답률</b>: 80점 이상 받은 문제의 비율입니다
                    </li>
                    <li>
                      <b>정답/오답 개수</b>: 80점 기준으로 정답과 오답이
                      구분됩니다
                    </li>
                  </ul>

                  <h4>🎖️ 등급 기준</h4>
                  <ul>
                    <li>
                      <b>S</b>: 95점 이상 - 완벽한 이해도
                    </li>
                    <li>
                      <b>A+</b>: 90점 이상 - 매우 높은 이해도
                    </li>
                    <li>
                      <b>A</b>: 85점 이상 - 높은 이해도
                    </li>
                    <li>
                      <b>B+</b>: 80점 이상 - 좋은 이해도
                    </li>
                    <li>
                      <b>B</b>: 75점 이상 - 양호한 이해도
                    </li>
                    <li>
                      <b>C+</b>: 70점 이상 - 기본적인 이해도
                    </li>
                    <li>
                      <b>C</b>: 65점 이상 - 보통 수준
                    </li>
                    <li>
                      <b>D</b>: 60점 이상 - 개선 필요
                    </li>
                    <li>
                      <b>F</b>: 60점 미만 - 많은 연습 필요
                    </li>
                  </ul>

                  <h4>🏆 승패 판정</h4>
                  <ul>
                    <li>
                      <b>승리</b>: 정답률 80% 이상
                    </li>
                    <li>
                      <b>무승부</b>: 정답률 50% 이상 80% 미만
                    </li>
                    <li>
                      <b>패배</b>: 정답률 50% 미만
                    </li>
                  </ul>

                  <h4>💪 강점/약점 분석</h4>
                  <ul>
                    <li>
                      <b>강점</b>: 정답한 문제의 난이도와 유형을 분석합니다
                    </li>
                    <li>
                      <b>약점</b>: 오답한 문제의 난이도와 유형을 분석합니다
                    </li>
                    <li>분석 결과를 바탕으로 개선 방향을 제시합니다</li>
                  </ul>

                  <h4>📚 다음 학습 추천</h4>
                  <ul>
                    <li>
                      현재 성적과 난이도를 고려하여 다음 학습 방향을 추천합니다
                    </li>
                    <li>틀린 문제는 오답 노트로 저장하여 복습하세요</li>
                  </ul>
                </div>

                <div className="battle-rule-section">
                  <h3>💡 팁</h3>
                  <ul>
                    <li>모든 문제를 완료해야 최종 결과가 결정됩니다</li>
                    <li>틀린 문제는 오답 노트로 저장하여 복습할 수 있습니다</li>
                  </ul>
                </div>
              </div>

              <div className="battle-rules-modal-footer">
                <button
                  className="battle-rules-modal-btn"
                  onClick={() => setShowRulesModal(false)}
                >
                  확인
                </button>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </motion.div>
  );
}

