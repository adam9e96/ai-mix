import React from "react";
import { motion } from "framer-motion";
import "../../styles/components/toggle.css";

/**
 * @typedef {Object} ToggleSwitchProps
 * @property {boolean} checked - 토글 상태 (true: 켜짐, false: 꺼짐)
 * @property {(checked: boolean) => void} onChange - 토글 상태 변경 핸들러
 * @property {string} [label] - 토글 라벨 (선택사항)
 * @property {boolean} [disabled] - 비활성화 여부
 */

/**
 * 재사용 가능한 토글 스위치 컴포넌트
 * @param {ToggleSwitchProps} props
 * @returns {JSX.Element}
 */
function ToggleSwitch({ checked, onChange, label, disabled = false }) {
  return (
    <div className="toggle-wrapper">
      {label && <span className="toggle-label">{label}</span>}
      <button
        className={`toggle-switch ${checked ? "toggle-on" : "toggle-off"} ${
          disabled ? "toggle-disabled" : ""
        }`}
        onClick={() => {
          if (!disabled && onChange) {
            onChange(!checked);
          }
        }}
        disabled={disabled}
        aria-label={label || "토글 스위치"}
        aria-checked={checked}
        role="switch"
      >
        <motion.div
          className="toggle-handle"
          animate={{
            x: checked ? 20 : 2,
          }}
          transition={{
            type: "spring",
            stiffness: 500,
            damping: 30,
          }}
        />
      </button>
    </div>
  );
}

export default ToggleSwitch;

