package com.carproject.car.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForMeAnswerDto {

    // 1) 탑승 인원 (1~6 이상 => 6으로 받자)
    private Integer passengers;

    // 2) 짐 적재량
    private LuggageLevel luggage;

    // 3) 예산
    private BudgetRange budget;

    // 4) 파워트레인(복수)
    private List<Powertrain> powertrains = new ArrayList<>();

    // 5) EV 충전 가능? (EV 선택시에만)
    private Boolean evChargingAvailable;

    // 6) 운전스타일/주행환경
    private DrivingEnvironment drivingEnv;

    // 7) 연비 중요도
    private FuelEconomyPriority fuelEconomyPriority;

    // 8) 국산/외제 선호 (BRAND.COUNTRY_CODE로 판정)
    private OriginPreference originPreference;

    // 9) 어시스턴트(ADAS) 옵션 중요도 (가점만, 감점 없음)
    private AssistantPriority assistantPriority;
}
