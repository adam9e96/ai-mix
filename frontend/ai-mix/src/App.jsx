import { Routes, Route } from "react-router-dom";
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";

import Layout from "./layout/Layout";
import Home from "./pages/Home";
import Login from "./pages/Login";
import SignUp from "./pages/SignUp";
import ChatPage from "./pages/Chat";
import "./styles/common/toast.css";
import MyPage from "./pages/mypage/MyPage";
import About from "./pages/About";
import QnaList from "./pages/qna/QnaList";
import QnaDetail from "./pages/qna/QnaDetail";
import QnaAsk from "./pages/qna/QnaAsk";
import BattleHistory from "./pages/mypage/BattleHistory";
import BattleDetail from "./pages/mypage/BattleDetail";
import EditProfile from "./pages/mypage/EditProfile";
import KnowledgePage from "./pages/knowledge/KnowledgeHome";
import KnowledgeDetail from "./pages/knowledge/KnowledgeDetail";

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
          <Route path="/mypage/battles/:battleId" element={<BattleDetail />} />

          <Route path="/knowledge" element={<KnowledgePage />} />
          <Route path="/knowledge/cards/:slug" element={<KnowledgeDetail />} />
        </Route>
      </Routes>
    </>
  );
}

export default App;
