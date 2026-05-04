import { memo } from "react";
import { Sword } from "lucide-react";

// React.memo: props가 변경되지 않으면 리렌더링 스킵
const BattleButton = memo(function BattleButton({ onClick }) {
  return (
    <button className="battle-btn" onClick={onClick}>
      <Sword size={18} />
      배틀하기
    </button>
  );
});

export default BattleButton;

