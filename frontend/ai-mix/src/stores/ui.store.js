// 모달, 토스트, 로딩, UI 상태
import { create } from "zustand";

export const useUIStore = create((set) => ({
  // 마이페이지 로딩 상태
  mypageLoading: false,
  setMypageLoading: (loading) => set({ mypageLoading: loading }),

  // 채팅 관련 UI 상태
  // 모달 상태
  showModal: false,
  modalType: "create", // "create" | "rename"
  showDeleteModal: false,
  showLoginModal: false,
  sessionToDelete: null,

  // 사이드바 상태
  sidebarOpen: true,

  // 드롭다운 상태
  dropdownId: null,

  // 배틀 모달 상태
  battleOpen: false,

  // 코드 복사 상태
  copiedCodeId: null,

  // 액션: 모달 열기/닫기
  setShowModal: (show) => set({ showModal: show }),
  setModalType: (type) => set({ modalType: type }),
  setShowDeleteModal: (show) => set({ showDeleteModal: show }),
  setShowLoginModal: (show) => set({ showLoginModal: show }),
  setSessionToDelete: (id) => set({ sessionToDelete: id }),

  // 액션: 사이드바 토글
  setSidebarOpen: (open) => set({ sidebarOpen: open }),

  // 액션: 드롭다운 관리
  setDropdownId: (id) => set({ dropdownId: id }),

  // 액션: 배틀 모달
  setBattleOpen: (open) => set({ battleOpen: open }),

  // 액션: 코드 복사 상태
  setCopiedCodeId: (id) => set({ copiedCodeId: id }),
}));
