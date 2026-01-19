import { create } from "zustand";

export const useKnowledgeStore = create((set) => ({
  /* =====================
     상태 (임시 mock)
  ===================== */
  totalCount: 1284,

  searchKeyword: "",
  selectedCategory: null,

  categories: [
    { id: 1, name: "프로그래밍" },
    { id: 2, name: "네트워크" },
    { id: 3, name: "알고리즘" },
  ],

  popularConcepts: [
    { id: 1, title: "REST API" },
    { id: 2, title: "HTTP 상태코드" },
    { id: 3, title: "JWT" },
  ],

  recentUpdates: [
    { id: 4, title: "HTTP v2.0" },
    { id: 5, title: "JWT v1.1" },
  ],

  wrongConcepts: ["REST = JSON ❌", "상태코드 200 = 성공만 ❌"],

  favorites: ["REST API"],

  /* =====================
     액션
  ===================== */
  setSearchKeyword: (keyword) => set({ searchKeyword: keyword }),

  selectCategory: (category) => set({ selectedCategory: category }),
}));
