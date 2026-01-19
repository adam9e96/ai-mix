import { Sword } from "lucide-react";

export default function BattleButton({ onClick }) {
  return (
    <button className="battle-btn" onClick={onClick}>
      <Sword size={18} />
      배틀하기
    </button>
  );
}

