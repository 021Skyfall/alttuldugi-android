# alttuldugi-android
Android app to manage Korean MVNO promo plans and remind optimal cancellation date.

# 알뜰뚜기 (Alttuldugi)

알뜰폰(MVNO) 메뚜기 요금제를 관리하고, 프로모션 종료/최소 유지 기간을 고려해  
손해 없이 해지·번호이동할 수 있는 시점을 계산해주는 안드로이드 앱입니다.

- 가입/개통일 + 프로모션 개월 수 기반으로 **프로모션 종료일** 계산
- 최소 유지 개월 수를 고려해 **추천 해지일** 계산
- 추천 해지일 기준 **N일 전(기본 15일)** 에 알림 발송
- 여러 회선을 한 번에 관리

자세한 기획/사양은 [`docs/mvno-hopper-spec.md`](docs/mvno-hopper-spec.md)를 참고하세요.
