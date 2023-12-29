# eShotLink

## Preset 
- Updated at `2019-02-18`
```
- Tool 신규 등록 시 basic shot을 등록하면 counter 설치(등록) 시 설치할 Tool을 선택하면 Preset항목에 basic shot으로 등록된 값이 자동 설정되고 해당 값으로 preset에 등록 됨.
- counter 등록 시 perset 값이 빈값으로 설정되어 등록하면 Preset 설정 없이 CDATA를 수정할 수 있도록 PresetStatus.APPLIED값으로 등록 됨.
- alert : MisconfigureAlertEvent => PresetStatus.READY 인 상태로 CDATA가 넘어오는 경우.
- 카운터 등록 후 추가 preset 설정은 Preset 메뉴에서 진행함.
```
- @25 Preset 페이지 변경. `Updated at 2019-02-18`
```
 - PRESET 신청 시점의 counter > shotCount 정보를 저장. (PRESET테이블 - Misconfigure alert 시 이후 증가분 만큼을 추가하여 제안함 )
 - 정보가 없는 경우 0으로 저장.
 
 - Admin > Preset 메뉴에서 Preset 정보 화면에서 
 - Last Preset Date, 
 - Lst Preset Shot(마지막 Preset 설정값)
 - Shots from last preset: Current Shots - Preset 설정 시점의 shotCount
 - Preset 제안 값 : last Preset Shots + Shots from last preset 
```

- @31 Preset 기능 추가  `Updated at 2019-02-22`
```Preset 기능 추가 
- Preset 설정 시 0 도 설정할 수 있도록 수정 
- Preset 설정을 취소할 수 있는 기능 추가
```