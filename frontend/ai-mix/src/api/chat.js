import instance from "./axiosInstance";

// ✅ 메시지 수정
export const updateChatMessageApi = (messageId, content) => {
  return instance.put(`/chat/messages/${messageId}`, {
    content,
  });
};

// ✅ 메시지 삭제
export const deleteChatMessageApi = (messageId) => {
  return instance.delete(`/chat/messages/${messageId}`);
};
