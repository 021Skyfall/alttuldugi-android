# alttuldugi-android
Android app to manage Korean MVNO promo plans and remind optimal cancellation date.

# 알뜰뚜기 (Alttuldugi)

알뜰폰(MVNO) 메뚜기 요금제를 관리하고, 프로모션 종료/최소 유지 기간을 고려해
손해 없이 해지·번호이동할 수 있는 시점을 계산해주는 안드로이드 앱입니다.

- 가입/개통일 + 프로모션 개월 수 기반으로 **프로모션 종료일** 계산
- 최소 유지 개월 수를 고려해 **추천 해지일** 계산
- 추천 해지일 기준 **N일 전(기본 15일)** 에 알림 발송
- 여러 회선을 한 번에 관리
- 통신망은 `KT 알뜰폰 / SK 알뜰폰 / LG 알뜰폰` 드롭다운으로 선택
- 사용 중인 실제 통신업체명과 요금제명은 직접 입력

자세한 기획/사양은 [`docs/mvno-hopper-spec.md`](docs/mvno-hopper-spec.md)를 참고하세요.

## 현재 상태

- Android Studio에서 열 수 있는 초기 프로젝트 골격 생성
- `Room`, `Lifecycle`, `WorkManager`, `Material3` 의존성 반영
- `DateCalculator`, `MobileService`, `Repository`, `HomeActivity` 기본 뼈대 추가
- 등록 화면에서 통신망/사용 통신업체명/요금제명 구조 반영
- 필수값 검증과 기본값 정책 반영
- 등록 화면 저장 기능과 홈 화면 건수 반영 연결
- 아직 목록/수정/상세/알림 실제 기능은 구현 전

## 시작 방법

1. Android Studio 최신 버전을 설치합니다.
2. `Open`으로 이 폴더를 그대로 엽니다.
3. Android SDK와 JDK 17 설정이 필요하면 Android Studio 안내에 따라 맞춥니다.
4. Gradle Sync를 실행합니다.
5. 에뮬레이터 또는 실기기를 연결한 뒤 앱을 실행합니다.

## 다음 추천 작업

1. `DateCalculator` 단위 테스트 추가
2. 요금제 등록 화면(`add_edit`) 구현
3. 메인 목록 화면에 RecyclerView 연결
4. 상세보기 화면과 알림 예약 연결
