## 📝 요약 (Summary)
- 예: 카톡 .txt 파일 비동기 파싱 엔진 및 RabbitMQ 연동 로직 구현

## 🚀 주요 변경 사항 (Changes)
- `MessageParser`: 정규표현식을 활용한 텍스트 데이터 구조화 로직 추가
- `RabbitMQConfig`: 비동기 처리를 위한 Queue 및 Exchange 설정
- `ChatDocument`: MongoDB에 저장될 메시지 스키마 설계

## 🧠 기술적 도전 및 의사결정 (Technical Challenge)
- **문제**: 수만 줄의 텍스트를 한 번에 파싱할 때 발생하는 메인 스레드 점유 이슈
- **해결**: **Kotlin Coroutines**의 `Dispatchers.IO`와 **RabbitMQ**를 조합하여 논블로킹(Non-blocking) 처리 구현

## ✅ 테스트 결과 (Verification)
- [ ] 단위 테스트(Unit Test) 통과 여부
- [ ] 통합 테스트(Integration Test) 통과 여부
- [ ] (선택) 10만 줄 샘플 데이터 파싱 성능 측정 결과

## 📂 관련 이슈 (Related Issues)
- Closes #이슈번호

## 📌 리뷰어 참고 사항
- 정규표현식 패턴이 아이폰/안드로이드 모두 대응되는지 한 번 더 확인 부탁드립니다.