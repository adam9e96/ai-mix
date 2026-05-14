import { lazy, Suspense } from "react";
import { Routes, Route } from "react-router-dom";
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import "./styles/common/toast.css";

import Layout from "./layout/Layout";
import LoadingDots from "./components/common/LoadingDots";

// 라우트별 코드 스플리팅 (React.lazy)
// 각 페이지가 별도 JS chunk으로 분리되어 해당 페이지 진입 시에만 로딩됨
const Home = lazy(() => import("./pages/Home"));
const Login = lazy(() => import("./pages/Login"));
const SignUp = lazy(() => import("./pages/SignUp"));
const About = lazy(() => import("./pages/About"));
const ChatPage = lazy(() => import("./pages/Chat"));
const MyPage = lazy(() => import("./pages/mypage/MyPage"));
const EditProfile = lazy(() => import("./pages/mypage/EditProfile"));
const QnaList = lazy(() => import("./pages/qna/QnaList"));
const QnaDetail = lazy(() => import("./pages/qna/QnaDetail"));
const QnaAsk = lazy(() => import("./pages/qna/QnaAsk"));
const BattleHistory = lazy(() => import("./pages/mypage/BattleHistory"));
const BattleDetail = lazy(() => import("./pages/mypage/BattleDetail"));
const KnowledgePage = lazy(() => import("./pages/knowledge/KnowledgeHome"));
const KnowledgeDetail = lazy(() => import("./pages/knowledge/KnowledgeDetail"));

function App() {
  return (
    <>
      <ToastContainer
        position="top-right"
        autoClose={2000}
        hideProgressBar={false}
        pauseOnHover
        theme="colored"
        style={{ zIndex: 10000 }}
      />

      {/* Suspense: lazy 컴포넌트 로딩 중 fallback UI 표시 */}
      <Suspense fallback={<LoadingDots />}>
        <Routes>
          <Route element={<Layout />}>
            <Route path="/" element={<Home />} />
            <Route path="/login" element={<Login />} />
            <Route path="/signup" element={<SignUp />} />
            <Route path="/about" element={<About />} />

            <Route path="/chat" element={<ChatPage />} />
            {/* 챗/세션용 라우트 */}
            <Route path="/chat/:sessionId" element={<ChatPage />} />
            <Route path="/mypage" element={<MyPage />} />
            <Route path="/mypage/edit" element={<EditProfile />} />

            <Route path="/qna" element={<QnaList />} />
            <Route path="/qna/question/:id" element={<QnaDetail />} />
            <Route path="/qna/ask" element={<QnaAsk />} />

            <Route path="/mypage/battle-history" element={<BattleHistory />} />
            <Route
              path="/mypage/battles/:battleId"
              element={<BattleDetail />}
            />

            <Route path="/knowledge" element={<KnowledgePage />} />
            <Route
              path="/knowledge/cards/:slug"
              element={<KnowledgeDetail />}
            />
          </Route>
        </Routes>
      </Suspense>
    </>
  );
}

export default App;
