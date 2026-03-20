# MVNO 메뚜기 요금제 관리 앱 (MVNO Hopper)
## 프로젝트 사양서 & 기획안

---

## 1. 프로젝트 개요

### 1.1 목적
- 알뜰폰(MVNO) 및 통신사 프로모션 요금제를 순환적으로 갈아타면서 **프로모션 종료일과 위약금 조건을 감안해 손해 없이 해지·번호이동할 최적 시점을 계산**하고, 그 **보름 전에 알림을 보내주는 개인용 안드로이드 앱**

### 1.2 핵심 기능
1. 회선/요금제별 가입 정보 관리 (가입/개통일, 프로모션 기간, 최소유지 조건 등)
2. **추천 해지일** 자동 계산 (프로모션 종료일 + 최소유지 조건 고려)
3. **해지 알림 자동 예약** (기본값: 종료일 −15일, 사용자 커스텀 가능)
4. 회선별 상태 대시보드 (경과 개월, 남은 기간, 알림 상태 등)

### 1.3 대상 사용자
- 개인 사용자 (본인 회선 1~3개 정도 관리)
- 기술 레벨: 일반인 (쉬운 UI/UX 필수)

### 1.4 개발 플랫폼
- **Android** (API 28 이상, 코틀린 권장)
- **로컬 DB**: Room (SQLite 기반)
- **알림**: WorkManager + AlarmManager (백그라운드 알림)
- **라이프사이클**: 초기 버전 = 안드로이드 전용 (iOS/웹은 나중 고려)

---

## 2. 데이터 모델

### 2.1 Entity: MobileService (회선/요금제)

| 필드명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| `id` | Long (Primary Key) | Y | 자동 생성 PK |
| `lineName` | String | Y | 회선 이름 (예: "메뚜기 1호", "알뜰폰-A") |
| `operatorName` | String | Y | 통신사/MVNO명 (예: "SKT", "LG U+", "메뚜기") |
| `planName` | String | Y | 요금제명 (예: "2024년 특가 4GB") |
| `activationDate` | LocalDate | Y | 가입/개통일 (yyyy-MM-dd) |
| `promotionMonths` | Int | Y | 프로모션 기간(개월) (예: 4, 6, 12, 24 등) |
| `minContractMonths` | Int | N | 최소유지 개월 수 (기본값: 0 = 제약 없음) |
| `earlyTerminationFee` | Int | N | 최소유지 이전 해지 시 위약금(원) (기본값: 0) |
| `monthlyFee` | Int | N | 월 요금(원) (정보 목적, 계산에 미사용) |
| `reminderDaysBeforeEnd` | Int | N | 알림 리드타임(일) (기본값: 15) |
| `notes` | String | N | 메모 (예: "다음달 5일 갈아탈 예정") |
| `createdAt` | LocalDateTime | Y | 레코드 생성 시간 (자동) |
| `updatedAt` | LocalDateTime | Y | 레코드 수정 시간 (자동) |

### 2.2 계산 필드 (DB 미저장, 앱에서 계산)

| 필드명 | 계산 식 | 설명 |
|--------|---------|------|
| `promotionEndDate` | `activationDate + promotionMonths개월 - 1일` | 프로모션 혜택을 받는 마지막 날 |
| `minContractEndDate` | `activationDate + minContractMonths개월 - 1일` | 최소유지 조건이 끝나는 날 (0이면 계산 안 함) |
| `recommendedTerminationDate` | `max(promotionEndDate, minContractEndDate)` | 손해 없이 해지 가능한 날 |
| `recommendedReminderDate` | `recommendedTerminationDate - reminderDaysBeforeEnd일` | 알림 발송 날짜 |
| `elapsedMonths` | 오늘 기준 가입 후 경과 개월 수 | 회선 나이 표시용 |
| `remainingPromotionDays` | `promotionEndDate - 오늘` | 프로모션 남은 일수 (음수면 이미 종료) |

---

## 3. 날짜 계산 로직 (의사코드)

### 3.1 프로모션 종료일 계산

```

함수: calculatePromotionEndDate(activationDate, promotionMonths)
입력:

- activationDate: LocalDate (가입/개통일)
- promotionMonths: Int (프로모션 기간, 개월)

동작:

1. promotionEndDate = activationDate + promotionMonths개월
2. 마지막 날로 조정 (달의 마지막 날이 다를 경우 처리)
3. return promotionEndDate - 1일  // 프로모션을 받는 '마지막 날'

예시:

- 가입: 2026-01-15, 프로모션: 4개월
→ 계산: 2026-01-15 + 4개월 = 2026-05-15
→ 반환: 2026-05-14 (5월 14일이 마지막 혜택일)
- 가입: 2026-01-31, 프로모션: 6개월
→ 계산: 2026-01-31 + 6개월 = 2026-07-31
→ 반환: 2026-07-30 (7월 30일이 마지막 혜택일)

```

### 3.2 최소유지 종료일 계산

```

함수: calculateMinContractEndDate(activationDate, minContractMonths)
입력:

- activationDate: LocalDate (가입/개통일)
- minContractMonths: Int (최소유지 개월 수)

동작:

1. 만약 minContractMonths == 0 이면 return null (제약 없음)
2. minContractEndDate = activationDate + minContractMonths개월 - 1일
3. return minContractEndDate

예시:

- 가입: 2026-03-20, 최소유지: 3개월
→ 계산: 2026-03-20 + 3개월 = 2026-06-20
→ 반환: 2026-06-19 (위약금 없이 해지 가능해지는 날)

```

### 3.3 추천 해지일 계산

```

함수: calculateRecommendedTerminationDate(
activationDate,
promotionMonths,
minContractMonths
)
입력:

- activationDate: LocalDate
- promotionMonths: Int
- minContractMonths: Int

동작:

1. promotionEndDate = calculatePromotionEndDate(activationDate, promotionMonths)
2. minContractEndDate = calculateMinContractEndDate(activationDate, minContractMonths)
3. 만약 minContractEndDate == null 이면
return promotionEndDate  // 최소유지 제약 없음
4. 만약 minContractEndDate > promotionEndDate 이면
return minContractEndDate  // 위약금이 우선 조건
5. 그 외
return promotionEndDate  // 프로모션 종료 먼저

예시:
Case 1) 프로모션 4개월, 최소유지 없음
- promotionEndDate: 2026-05-14
- minContractEndDate: null
- 추천 해지일: 2026-05-14

Case 2) 프로모션 6개월, 최소유지 3개월
- activationDate: 2026-03-20
- promotionEndDate: 2026-09-18
- minContractEndDate: 2026-06-19
- 추천 해지일: 2026-09-18 (프로모션이 더 길어서)

Case 3) 프로모션 4개월, 최소유지 12개월
- activationDate: 2026-01-10
- promotionEndDate: 2026-05-09
- minContractEndDate: 2027-01-09
- 추천 해지일: 2027-01-09 (위약금이 더 길어서)

```

### 3.4 알림 발송 날짜 계산

```

함수: calculateReminderDate(recommendedTerminationDate, reminderDaysBeforeEnd)
입력:

- recommendedTerminationDate: LocalDate (추천 해지일)
- reminderDaysBeforeEnd: Int (알림 리드타임, 기본값: 15)

동작:

1. reminderDate = recommendedTerminationDate - reminderDaysBeforeEnd일
2. return reminderDate

예시:

- 추천 해지일: 2026-05-14, 리드타임: 15일
→ 알림 발송: 2026-04-29
- 추천 해지일: 2026-09-18, 리드타임: 7일
→ 알림 발송: 2026-09-11

```

### 3.5 경과 개월 계산

```

함수: calculateElapsedMonths(activationDate, today)
입력:

- activationDate: LocalDate (가입일)
- today: LocalDate (오늘 날짜)

동작:

1. months = ChronoUnit.MONTHS.between(activationDate, today)
2. return months

예시:

- 가입: 2026-01-15, 오늘: 2026-03-20
→ 경과: 2개월

```

### 3.6 프로모션 남은 일수 계산

```

함수: calculateRemainingPromotionDays(promotionEndDate, today)
입력:

- promotionEndDate: LocalDate
- today: LocalDate (오늘 날짜)

동작:

1. days = ChronoUnit.DAYS.between(today, promotionEndDate)
2. return days (음수면 이미 종료)

예시:

- 프로모션 종료: 2026-05-14, 오늘: 2026-03-20
→ 남은 일수: 55일
- 프로모션 종료: 2026-02-10, 오늘: 2026-03-20
→ 남은 일수: -39일 (이미 종료)

```

---

## 4. UI/UX 플로우

### 4.1 화면 구성

#### 4.1.1 메인 화면 (회선 목록)
```

┌─────────────────────────────────┐
│  📱 알뜰폰 메뚜기 관리          │
├─────────────────────────────────┤
│                                 │
│  [+ 회선 추가]                  │
│                                 │
│  ┌─ 회선 1: 메뚜기 1호 ──────┐ │
│  │ 통신사: SKT MVNO          │ │
│  │ 요금제: 4GB 특가          │ │
│  │ 가입일: 2026-01-15       │ │
│  │ 경과: 2개월, 남은 기간: 55일 │ │
│  │ ✅ 알림 설정됨 (4월 29일) │ │
│  │ [상세보기] [수정] [삭제]  │ │
│  └──────────────────────────┘ │
│                                 │
│  ┌─ 회선 2: 통신사 할인 ──────┐ │
│  │ 통신사: LG U+             │ │
│  │ 요금제: 6GB 프로모션      │ │
│  │ 가입일: 2025-10-30       │ │
│  │ 경과: 5개월, 남은 기간: -105일 │ │
│  │ ⚠️ 프로모션 종료됨!       │ │
│  │ [상세보기] [수정] [삭제]  │ │
│  └──────────────────────────┘ │
│                                 │
└─────────────────────────────────┘

```

#### 4.1.2 회선 상세보기 화면
```

┌──────────────────────────────────┐
│  📱 메뚜기 1호 - 상세정보        │
├──────────────────────────────────┤
│                                  │
│  기본 정보                        │
│  ─────────────────────────────  │
│  회선명: 메뚜기 1호              │
│  통신사: SKT MVNO                │
│  요금제: 4GB 특가                │
│  월 요금: 12,900원               │
│                                  │
│  일정 정보                        │
│  ─────────────────────────────  │
│  가입/개통일: 2026-01-15        │
│  프로모션 기간: 4개월             │
│  프로모션 종료일: 2026-05-14     │
│  경과: 2개월 (66%)               │
│  남은 기간: 55일                  │
│                                  │
│  조건/제약                        │
│  ─────────────────────────────  │
│  최소유지 기간: 없음              │
│  위약금: 없음                    │
│  추천 해지일: 2026-05-14        │
│                                  │
│  알림 설정                        │
│  ─────────────────────────────  │
│  알림 여부: ✅ 활성화             │
│  알림 리드타임: 15일 전           │
│  알림 예정일: 2026-04-29        │
│  메모: 다음달 SKT로 갈아탈 예정  │
│                                  │
│  [수정] [삭제] [번호이동 가이드] │
│                                  │
└──────────────────────────────────┘

```

#### 4.1.3 회선 등록/수정 화면
```

┌─────────────────────────────────┐
│  ➕ 회선 추가                    │
├─────────────────────────────────┤
│                                 │
│  회선명 *                        │
│  [메뚜기 1호____________]        │
│  예: "메뚜기 1호", "SKT 할인"    │
│                                 │
│  통신사/MVNO명 *                 │
│  [SKT________________]           │
│  예: "SKT", "LG U+", "메뚜기"    │
│                                 │
│  요금제명                        │
│  [4GB 특가____________]          │
│                                 │
│  가입/개통일 *                   │
│  [2026-01-15         📅]       │
│                                 │
│  프로모션 기간(개월) *           │
│  [4                  ▼]        │
│  (숫자 입력, 드롭다운 또는 자유입력) │
│                                 │
│  최소유지 기간(개월)             │
│  [0                  ▼]        │
│  (0 = 제약 없음)                 │
│                                 │
│  위약금(원)                      │
│  [0                  ]          │
│                                 │
│  월 요금(원) [선택사항]          │
│  [12900              ]          │
│                                 │
│  알림 리드타임(일) *             │
│  [15                 ▼]        │
│  (기본: 15일, 커스텀 가능)      │
│                                 │
│  메모                           │
│  [____________________]         │
│  [____________________]         │
│                                 │
│  [저장] [취소]                   │
│                                 │
└─────────────────────────────────┘

```

#### 4.1.4 알림 예시
```

┌────────────────────────────────┐
│ 📲 알뜰폰 메뚜기 관리          │
├────────────────────────────────┤
│                                │
│ 🔔 메뚜기 1호 - 갈아탈 준비!  │
│                                │
│ 📅 2026-05-14에 프로모션 종료  │
│ ⏰ 지금부터 새 요금제 찾기 시작! │
│ 💡 보름 안에 번호이동하세요.   │
│                                │
│ [자세히보기] [닫기]            │
│                                │
└────────────────────────────────┘

```

### 4.2 사용자 플로우

```

1. 앱 실행
↓
2. 메인 화면 (회선 목록) 표시
↓
3. "+ 회선 추가" 터치
↓
4. 등록 폼 화면 이동
    - 회선명, 통신사, 요금제, 가입일, 프로모션 기간,
최소유지, 위약금, 월 요금(선택), 알림 리드타임 입력
↓
5. [저장] 터치
    - 계산 실행: 프로모션 종료일, 추천 해지일, 알림일 자동 계산
    - DB 저장
    - WorkManager로 알림 예약
↓
6. 메인 화면으로 돌아감
    - 새로운 회선이 목록에 추가됨
    - 상태(경과/남은기간/알림상태) 표시
↓
7. 사용자가 회선을 터치하면 상세보기 화면
    - 모든 계산값, 조건, 알림 설정 표시
↓
8. 알림 발송 (최소유지 또는 프로모션 종료 15일 전)
    - 푸시 알림 + 앱 배지 표시
↓
9. 알림을 탭하면 해당 회선 상세보기로 이동
    - 추천 해지일/번호이동 가이드 표시
```

---

## 5. 기술 스택 & 아키텍처

### 5.1 개발 환경
- **언어**: Kotlin (권장) 또는 Java
- **Min SDK**: Android API 28 (Android 9.0)
- **Target SDK**: API 34 이상
- **IDE**: Android Studio

### 5.2 주요 라이브러리
| 기능 | 라이브러리 | 용도 |
|------|----------|------|
| DB | Room (androidx.room:room-*) | 로컬 데이터 저장 |
| 날짜 계산 | java.time (LocalDate) 또는 Joda-Time | 날짜 연산 |
| 백그라운드 작업 | WorkManager (androidx.work:work-runtime-ktx) | 주기적 알림 실행 |
| 알림 | NotificationManager + PendingIntent | 푸시 알림 발송 |
| UI | Material3 (androidx.material3) | Modern Material Design |
| Lifecycle | androidx.lifecycle | ViewModel, LiveData |

### 5.3 아키텍처 패턴
- **MVVM** (Model-View-ViewModel)
  - **Model**: Room Entity + Repository
  - **ViewModel**: 계산 로직 + 상태 관리
  - **View**: Activity/Fragment + Data Binding

### 5.4 패키지 구조 (예시)

```

com.mvnohopper
├── data
│   ├── entity
│   │   └── MobileService.kt
│   ├── dao
│   │   └── MobileServiceDao.kt
│   ├── database
│   │   └── MvnoHopperDatabase.kt
│   └── repository
│       └── MobileServiceRepository.kt
├── domain
│   ├── model
│   │   └── MobileServiceWithCalculations.kt
│   └── usecase
│       └── CalculateDateUseCase.kt
├── presentation
│   ├── ui
│   │   ├── home
│   │   │   ├── HomeActivity.kt
│   │   │   ├── LineListAdapter.kt
│   │   │   └── home_fragment.xml
│   │   ├── detail
│   │   │   ├── DetailActivity.kt
│   │   │   └── detail_fragment.xml
│   │   ├── add_edit
│   │   │   ├── AddEditActivity.kt
│   │   │   └── add_edit_fragment.xml
│   │   └── common
│   │       └── DatePickerHelper.kt
│   ├── viewmodel
│   │   ├── HomeViewModel.kt
│   │   ├── DetailViewModel.kt
│   │   └── AddEditViewModel.kt
│   └── adapter
│       └── LineListAdapter.kt
├── notification
│   ├── NotificationWorker.kt
│   └── NotificationManager.kt
├── util
│   ├── DateCalculator.kt
│   ├── Constants.kt
│   └── Extensions.kt
└── MvnoHopperApp.kt (Application class)

```

---

## 6. 기능 명세

### 6.1 회선 등록
- **입력**: 회선명, 통신사명, 요금제명, 가입일, 프로모션 기간, 최소유지, 위약금, 월 요금(선택), 알림 리드타임
- **처리**: 
  1. 모든 필수 필드 검증
  2. 계산 실행 (프로모션 종료일, 추천 해지일, 알림일)
  3. DB 저장
  4. WorkManager로 알림 예약
- **출력**: 등록 성공 메시지 + 메인 화면 이동

### 6.2 회선 수정
- **입력**: 기존 회선 + 수정 사항
- **처리**:
  1. 필드 검증
  2. 계산 재실행
  3. DB 업데이트
  4. 기존 알림 취소 + 새로운 알림 재예약
- **출력**: 수정 성공 메시지 + 상세보기 화면

### 6.3 회선 삭제
- **입력**: 삭제 대상 회선
- **처리**:
  1. 예약된 알림 취소
  2. DB 레코드 삭제
- **출력**: 삭제 성공 메시지 + 메인 화면 이동

### 6.4 메인 화면 표시
- **처리**:
  1. DB에서 모든 회선 조회
  2. 각 회선별 계산값 실시간 계산 (경과 개월, 남은 기간, 알림 상태)
  3. 정렬: 추천 해지일 순서 (임박 순서)
  4. 색상 코딩:
     - 🟢 초록: 프로모션 진행 중 (남은 기간 > 30일)
     - 🟡 노랑: 프로모션 임박 (남은 기간 7~30일)
     - 🔴 빨강: 프로모션 종료 또는 알림 예정 (남은 기간 < 7일 또는 이미 종료)
- **출력**: 회선 목록 + 상태 표시

### 6.5 알림 발송
- **트리거**: WorkManager (Daily 반복 작업)
- **처리**:
  1. 매일 자정에 실행
  2. 모든 회선 확인
  3. 오늘 = 알림 예정일이면 푸시 알림 발송
  4. 알림 클릭 → 해당 회선 상세보기 이동
- **알림 내용**:
```

[제목] 메뚜기 1호 - 갈아탈 준비!
[본문] 2026-05-14에 프로모션 종료. 새 요금제를 찾아보세요!

```

---

## 7. 데이터 검증 & 제약 조건

### 7.1 필수 필드
- `lineName`: 길이 1~50자, 특수문자 제외
- `operatorName`: 길이 1~30자
- `activationDate`: 과거 날짜 (오늘 이후 선택 불가)
- `promotionMonths`: 1 이상 정수

### 7.2 선택 필드 (기본값)
- `minContractMonths`: 기본 0 (제약 없음)
- `earlyTerminationFee`: 기본 0원
- `monthlyFee`: 기본 0원 (정보용)
- `reminderDaysBeforeEnd`: 기본 15일 (사용자 커스텀 1~60일 범위)

### 7.3 비즈니스 로직 검증
- `minContractMonths` > `promotionMonths`인 경우 허용 (위약금 조건이 더 긴 경우)
- `activationDate` + `promotionMonths` = 미래 날짜 확인 (과거 프로모션 마감 회선도 관리 가능하되, UI에서 경고)

---

## 8. 초기 버전 범위 (MVP)

### 포함 기능
- ✅ 회선 등록/수정/삭제 (CRUD)
- ✅ 프로모션/추천 해지일 자동 계산
- ✅ 메인 화면 목록 + 상태 표시
- ✅ 상세보기 화면
- ✅ 로컬 DB 저장 (Room)
- ✅ 알림 예약 + 발송 (WorkManager)
- ✅ 기본적인 입력 검증

### 제외 기능 (나중 버전)
- ❌ 클라우드 동기화 (구글 계정 연동 등)
- ❌ 요금제 추천 AI
- ❌ 번호이동 자동화
- ❌ 통계/차트 분석
- ❌ iOS 지원
- ❌ 가족/다중 사용자 관리
- ❌ 복잡한 약정 공식 계산

---

## 9. 개발 우선순위 (에이전트용)

### Phase 1: 기반 구축 (1주)
1. Room Entity + DAO + Database 설정
2. Repository 패턴 구현
3. DateCalculator 유틸리티 (모든 계산 함수)
4. 기본 Activity/Fragment 틀

### Phase 2: UI 구현 (1주)
1. 메인 화면 (리스트 + 어댑터)
2. 등록/수정 폼 화면
3. 상세보기 화면
4. 기본 스타일 (Material3)

### Phase 3: 비즈니스 로직 (1주)
1. ViewModel + 계산 로직 통합
2. DB 저장/로드/삭제 동작 확인
3. 입력 검증 + 에러 핸들링
4. 리스트 정렬 + 색상 코딩

### Phase 4: 알림 & 마무리 (1주)
1. WorkManager + AlarmManager 연동
2. 푸시 알림 발송 구현
3. 알림 클릭 핸들링
4. 디버깅 + 테스트

---

## 10. 테스트 및 검증

### 10.1 단위 테스트 (Unit Test)
```kotlin
// 예: DateCalculator 테스트
@Test
fun testPromotionEndDate_4Months() {
  val activation = LocalDate.of(2026, 1, 15)
  val result = DateCalculator.calculatePromotionEndDate(activation, 4)
  assertEquals(LocalDate.of(2026, 5, 14), result)
}

@Test
fun testRecommendedTerminationDate_WithLongerMinContract() {
  val activation = LocalDate.of(2026, 1, 10)
  val result = DateCalculator.calculateRecommendedTerminationDate(
      activation, 
      promotionMonths = 4,
      minContractMonths = 12
  )
  assertEquals(LocalDate.of(2027, 1, 9), result)
}
```


### 10.2 통합 테스트

- DB 저장 → 조회 → 계산값 검증
- 회선 등록 → 알림 예약 → 알림 발송 흐름


### 10.3 수동 테스트 (QA)

1. 회선 1개 등록: 프로모션 4개월, 최소유지 없음
    - ✓ 추천 해지일 = 프로모션 종료일
    - ✓ 알림일 = 종료일 − 15일
    - ✓ 메인 화면에 녹색 표시
    - ✓ 알림 예약 확인
2. 회선 2개 등록: 하나는 이미 종료됨
    - ✓ 메인 화면 빨간색 표시
    - ✓ 상세보기에서 "프로모션 종료됨" 표시
3. 알림 리드타임 커스텀 (7일)
    - ✓ 등록 시 커스텀값 저장 확인
    - ✓ 알림일 재계산 확인
4. 회선 삭제
    - ✓ DB에서 제거됨
    - ✓ 예약된 알림 취소됨

---

## 11. 기대 효과

- 👍 프로모션 종료일을 자동으로 추적해서 **수동 계산 불필요**
- 👍 최소유지/위약금 조건 자동 검토해서 **손해 최소화**
- 👍 보름 전 미리 알림을 받아서 **새 요금제 탐색 준비 시간 확보**
- 👍 여러 회선을 한 곳에서 관리해서 **메뚜기 순환 체계화**
- 👍 개인용 로컬 앱이라 **클라우드 의존성 없고 비용 0**

---

## 12. 향후 고도화 아이디어 (v2.0 이상)

- 📊 월별 요금 절약액 통계
- 🔔 알림 히스토리 (지난 알림 목록)
- 🏷️ 태그/카테고리 (회사용/개인용 구분)
- 📱 위젯 (홈 화면에서 다음 갈아탈 날짜 표시)
- 🌐 클라우드 백업 (선택사항)
- 👥 가족 계정 공유 (권한 관리)
- 🤖 AI 요금제 추천 (외부 API 연동)
- 📋 번호이동 체크리스트 (기관별 번호 안내)
