import { Outlet } from "react-router-dom";
import Header from "../components/common/Header";
import Footer from "../components/common/Footer";
import "../styles/layout/layout.css";

function Layout() {
  return (
    <div className="app-layout">
      <Header />

      {/* 페이지 내용이 들어가는 자리 */}
      <main className="app-main">
        <Outlet />
      </main>

      <Footer />
    </div>
  );
}

export default Layout;
