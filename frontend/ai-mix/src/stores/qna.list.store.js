// src/stores/qna.store.js
import { create } from "zustand";
import instance from "@/api/axiosInstance";

export const useQnaStore = create((set, get) => ({
  list: [],
  loading: false,
  pageInfo: {
    page: 0,
    size: 5,
    totalPages: 1,
  },
  searchType: "title", // "title", "author", "content"
  searchKeyword: "",
  selectedTags: [], // 선택된 태그 필터

  setSearchType: (type) => set({ searchType: type }),
  setSearchKeyword: (keyword) => set({ searchKeyword: keyword }),
  setSelectedTags: (tags) =>
    set({ selectedTags: Array.isArray(tags) ? tags : [] }),
  addSelectedTag: (tag) =>
    set((state) => {
      const tags = Array.isArray(state.selectedTags) ? state.selectedTags : [];
      if (tag && !tags.includes(tag)) {
        return { selectedTags: [...tags, tag] };
      }
      return state;
    }),
  removeSelectedTag: (tag) =>
    set((state) => {
      const tags = Array.isArray(state.selectedTags) ? state.selectedTags : [];
      return { selectedTags: tags.filter((t) => t !== tag) };
    }),

  loadQnaList: async (page = 0, searchType = null, searchKeyword = null) => {
    try {
      set({ loading: true });

      // 스토어의 현재 검색값 사용 (파라미터가 없으면)
      const currentSearchType = searchType ?? get().searchType;
      const currentSearchKeyword = searchKeyword ?? get().searchKeyword;
      const selectedTags = get().selectedTags || [];

      let res;

      // 태그 필터가 있으면 태그 검색 API 사용 (우선순위)
      if (selectedTags.length > 0) {
        // 태그 검색 API: GET /api/v1/qna/questions/search?keyword=태그1,태그2,태그3&searchType=tag&page=0&size=5
        // 여러 태그가 선택된 경우 쉼표로 구분하여 전달
        const validTags = selectedTags.filter((t) => t && t.trim());
        const tagKeyword = validTags.map((t) => t.trim()).join(",");
        const queryParams = new URLSearchParams({
          keyword: tagKeyword,
          searchType: "tag",
          page: page.toString(),
          size: "5",
          sortBy: "createdAt",
          sortDir: "DESC",
        });
        res = await instance.get(`/qna/questions/search?${queryParams}`);
      } else if (currentSearchKeyword && currentSearchKeyword.trim()) {
        // 검색어가 있으면 검색 API 사용
        // 검색 API: GET /api/v1/qna/questions/search?keyword=검색어&searchType=검색타입&page=0&size=5&sortBy=createdAt&sortDir=DESC
        // searchType: title(기본값), author, content
        const queryParams = new URLSearchParams({
          keyword: currentSearchKeyword.trim(),
          searchType: currentSearchType || "title",
          page: page.toString(),
          size: "5",
          sortBy: "createdAt",
          sortDir: "DESC",
        });
        res = await instance.get(`/qna/questions/search?${queryParams}`);
      } else {
        // 일반 목록 API: GET /api/v1/qna/questions?page=0&size=5&sortBy=createdAt&sortDir=DESC
        const queryParams = new URLSearchParams({
          page: page.toString(),
          size: "5",
          sortBy: "createdAt",
          sortDir: "DESC",
        });
        res = await instance.get(`/qna/questions?${queryParams}`);
      }

      // Spring Page 객체 구조:
      // - 기본: { content: [...], number, size, totalPages, ... }
      // - 현재 API: { content: [...], page: { number, size, totalPages, ... } }
      // 참고: gptSummary 필드는 백엔드에서 제거되었으므로 프론트엔드에서 고려하지 않음
      const data = res.data || {};
      const pageData = data.page || data;
      const content = data.content || pageData.content || [];

      // 디버깅: 백엔드 응답 구조 확인
      if (import.meta.env.DEV) {
        console.log("백엔드 응답 데이터:", {
          전체응답: res.data,
          content: content.length,
          number: pageData.number,
          size: pageData.size,
          totalPages: pageData.totalPages,
          totalElements: pageData.totalElements,
          응답키목록: Object.keys(data),
        });
      }

      const pageInfo = {
        // 서버에서 값이 없거나 이상하게 와도 숫자로 강제 변환해서 NaN 표시 방지
        page: Number.isFinite(Number(pageData.number))
          ? Number(pageData.number)
          : 0,
        size: Number.isFinite(Number(pageData.size))
          ? Number(pageData.size)
          : 5,
        totalPages:
          Number.isFinite(Number(pageData.totalPages)) &&
          Number(pageData.totalPages) > 0
            ? Number(pageData.totalPages)
          : 1,
      };

      // 디버깅: 페이지네이션 정보 확인
      if (import.meta.env.DEV) {
        console.log("페이지네이션 정보:", {
          현재페이지: pageInfo.page + 1,
          전체페이지: pageInfo.totalPages,
          페이지크기: pageInfo.size,
          아이템수: content.length,
          전체아이템수: pageData.totalElements || 0,
        });
      }

      set({
        list: content,
        pageInfo,
      });
    } catch (e) {
      console.error("QnA 목록 조회 실패", e);
    } finally {
      set({ loading: false });
    }
  },
}));
