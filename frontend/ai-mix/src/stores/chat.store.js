// 채팅 세션 및 메시지 관리
import { create } from "zustand";
import axiosInstance from "../api/axiosInstance";
import { toast } from "react-toastify";

export const useChatStore = create((set, get) => ({
  // 상태
  sessions: [],
  currentSession: null,
  messages: { temp: [] },
  input: "",
  loading: false,
  newTitle: "", // 세션 생성/수정용 제목

  // 액션: 세션 목록 가져오기
  fetchSessions: async () => {
    try {
      const res = await axiosInstance.get("/chat/sessions");
      set({
        sessions: res.data.sessions.map((s) => ({
          id: s.sessionId,
          title: s.title,
        })),
      });
    } catch (err) {
      console.error(err);
    }
  },

  // 액션: 세션 메시지 로드
  loadMessages: async (id) => {
    try {
      const res = await axiosInstance.get(`/chat/sessions/${id}/messages`);

      const list = res.data.messages.map((msg) => ({
        id: msg.id,
        role: msg.sender === "USER" ? "user" : "bot",
        text: msg.message,
      }));

      set((state) => ({
        messages: { ...state.messages, [id]: list },
        currentSession: id,
      }));
    } catch (err) {
      console.error(err);
    }
  },

  // 액션: 세션 생성 (제목만)
  createSession: async (title) => {
    if (!title.trim()) {
      // 제목이 없으면 기존 동작
      set({ messages: { temp: [] }, currentSession: null, newTitle: "" });
      return null;
    }

    try {
      const res = await axiosInstance.post(
        "/chat",
        {
          sessionId: null,
          message: "",
          title: title.trim(),
        },
        { withCredentials: true }
      );

      const newSessionId = res.data.sessionId;
      set({
        currentSession: newSessionId,
        messages: { temp: [] },
        newTitle: "",
      });

      // 세션 목록 새로고침
      await get().fetchSessions();

      return newSessionId;
    } catch (err) {
      console.error("채팅방 생성 실패:", err);
      // 실패 시 기존 동작 유지
      set({ messages: { temp: [] }, currentSession: null, newTitle: "" });
      return null;
    }
  },

  // 액션: 세션 삭제
  deleteSession: async (sessionId) => {
    if (!sessionId) return;

    try {
      await axiosInstance.delete(`/chat/sessions/${sessionId}`);

      const { currentSession } = get();

      // 현재 세션이 삭제된 세션이면 초기화
      if (currentSession === sessionId) {
        set({
          currentSession: null,
          messages: { temp: [] },
        });
        window.history.replaceState(null, "", `/chat`);
      } else {
        // 메시지 상태에서도 삭제
        set((state) => {
          const newMessages = { ...state.messages };
          delete newMessages[sessionId];
          return { messages: newMessages };
        });
      }

      await get().fetchSessions();
      toast.success("채팅방이 삭제되었습니다");
    } catch (err) {
      console.error(err);
      toast.error("채팅방 삭제에 실패했습니다");
    }
  },

  // 액션: 세션 제목 수정
  renameSession: async (sessionId, title) => {
    if (!title.trim()) return;

    try {
      await axiosInstance.put(`/chat/sessions/${sessionId}`, {
        title: title.trim(),
      });

      await get().fetchSessions();
    } catch (err) {
      console.error("세션 제목 수정 실패:", err);
    }
  },

  // 액션: GPT 메시지 전송
  sendToGPT: async (text, currentKey, loadingMsgId) => {
    try {
      set({ loading: true });

      const { currentSession, newTitle } = get();
      const titleToSend =
        currentSession === null ? newTitle.trim() || null : null;

      const res = await axiosInstance.post(
        "/chat",
        {
          sessionId: currentSession,
          message: text,
          title: titleToSend,
        },
        { withCredentials: true }
      );

      let newId = currentSession;

      if (!currentSession) {
        newId = res.data.sessionId;
        set({ currentSession: newId, newTitle: "" }); // 세션 생성 후 제목 초기화

        // 현재 메시지 상태를 새 세션 ID로 복사
        set((state) => {
          const messagesToCopy = state.messages[currentKey] || [];
          return {
            messages: {
              ...state.messages,
              [newId]: [...messagesToCopy],
            },
          };
        });

        await get().fetchSessions();
        window.history.replaceState(null, "", `/chat/${newId}`);
      }

      return { answer: res.data.answer, sessionId: newId };
    } catch (err) {
      console.error(err);
      return {
        answer: "서버 오류 발생",
        sessionId: get().currentSession,
      };
    } finally {
      set({ loading: false });
    }
  },

  // 액션: 메시지 전송 (전체 플로우)
  onSend: async (isAuthenticated) => {
    if (!isAuthenticated) {
      // 로그인 체크는 호출하는 쪽에서 처리하므로 여기서는 단순히 return
      return;
    }

    const { input, currentSession, sendToGPT } = get();
    if (!input.trim()) return;

    const text = input.trim();
    set({ input: "" });

    const key = currentSession ?? "temp";

    // 1) 유저 메시지 추가
    const userMsgId = Date.now();
    set((state) => ({
      messages: {
        ...state.messages,
        [key]: [
          ...(state.messages[key] || []),
          { id: userMsgId, role: "user", text },
        ],
      },
    }));

    // 2) GPT 로딩(...) 메시지를 먼저 추가 (즉시 뜸)
    const loadingMsgId = Date.now() + 1;
    set((state) => ({
      messages: {
        ...state.messages,
        [key]: [
          ...(state.messages[key] || []),
          {
            id: loadingMsgId,
            role: "bot",
            text: "",
            typing: true, // LoadingDots() 표시됨
          },
        ],
      },
    }));

    // 3) GPT 호출
    const { answer, sessionId } = await sendToGPT(text, key, loadingMsgId);

    // 4) loading(...) → 타이핑 되는 실제 GPT 응답으로 교체
    set((state) => {
      const targetKey = sessionId ?? key;
      if (!state.messages[targetKey]) return state;

      return {
        messages: {
          ...state.messages,
          [targetKey]: state.messages[targetKey].map((m) =>
            m.id === loadingMsgId
              ? { ...m, text: answer, typing: false } // TypingText가 읽어서 타이핑 출력
              : m
          ),
        },
      };
    });
  },

  // 액션: 현재 세션 설정
  setCurrentSession: (sessionId) => set({ currentSession: sessionId }),

  // 액션: 입력값 설정
  setInput: (text) => set({ input: text }),

  // 액션: 새 제목 설정
  setNewTitle: (title) => set({ newTitle: title }),
}));
