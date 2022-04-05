import { useEffect, useRef, useState } from "react";

interface TimerProps {
  time: number;
}

export default function Timer({ time }: TimerProps) {
  // 타이머를 초단위로 변환한 initialTime과 setInterval을 저장할 ref
  const initialTime = useRef(time * 60);
  const interval: any = useRef(null);

  const [min, setMin] = useState(0);
  const [sec, setSec] = useState(0);

  // 1초마다 1씩 줄이면서, 분,초 재설정하고 setInterval을 멈추는 조건으로 1초 지정
  // useEffect 안에서 변경한 변수는 유지 안되므로, useRef로 최초 시간 기억
  // parseInt는 문자열을 숫자로
  useEffect(() => {
    interval.current = setInterval(() => {
      initialTime.current -= 1;
      setSec(Number(initialTime.current % 60));
      setMin(Math.floor(initialTime.current / 60));
    }, 1000);
    return () => clearInterval(interval.current);
  }, []);

  // 초가 바뀔 때마다 실행
  useEffect(() => {
    if (initialTime.current <= 0) {
      clearInterval(interval.current);
    }
  }, [sec]);

  return (
    <div>
      {min}:{sec}
    </div>
  );
}
