import { useState } from "react";
import { updateChatMessageApi, deleteChatMessageApi } from "../../api/chat";
import { toast } from "react-toastify";

export default function ChatMessageItem({ message, isMine, onRefresh }) {
  const [isEdit, setIsEdit] = useState(false);
  const [editContent, setEditContent] = useState(message.content);

  // ✅ 수정
  const handleUpdate = async () => {
    try {
      await updateChatMessageApi(message.id, editContent);
      toast.success("메시지 수정 완료");
      setIsEdit(false);
      onRefresh();
    } catch {
      toast.error("메시지 수정 실패");
    }
  };

  // ✅ 삭제
  const handleDelete = async () => {
    if (!confirm("메시지를 삭제하시겠습니까?")) return;

    try {
      await deleteChatMessageApi(message.id);
      toast.success("메시지 삭제 완료");
      onRefresh();
    } catch {
      toast.error("메시지 삭제 실패");
    }
  };

  return (
    <div className={`chat-message ${isMine ? "mine" : ""}`}>
      {isEdit ? (
        <div className="edit-box">
          <input
            value={editContent}
            onChange={(e) => setEditContent(e.target.value)}
          />
          <button onClick={handleUpdate}>저장</button>
          <button onClick={() => setIsEdit(false)}>취소</button>
        </div>
      ) : (
        <>
          <span>{message.content}</span>

          {isMine && (
            <div className="message-actions">
              <button onClick={() => setIsEdit(true)}>수정</button>
              <button onClick={handleDelete}>삭제</button>
            </div>
          )}
        </>
      )}
    </div>
  );
}

