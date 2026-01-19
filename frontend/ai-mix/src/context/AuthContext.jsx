import { useEffect } from "react";
import { AuthContext } from "./AuthContextValue";
import { useAuthStore } from "../stores/auth.store";

export function AuthProvider({ children }) {
  // user와 loading 상태만 선택적으로 구독 (subscribeWithSelector 활용)
  const user = useAuthStore((state) => state.user);
  const loading = useAuthStore((state) => state.loading);
  const setUser = useAuthStore((state) => state.setUser);

  // ⭐ 앱 시작 시 자동으로 loadUser() 호출하지 않음
  // 필요한 곳(로그인 후 등)에서만 호출하도록 변경
  // → 로그인하지 않은 상태에서 불필요한 403 에러 요청 방지
  useEffect(() => {
    // loading 상태를 false로 초기화 (요청하지 않으므로)
    const currentLoading = useAuthStore.getState().loading;
    if (currentLoading) {
      useAuthStore.setState({ loading: false });
    }
  }, []);

  return (
    <AuthContext.Provider value={{ user, setUser, loading }}>
      {children}
    </AuthContext.Provider>
  );
}
