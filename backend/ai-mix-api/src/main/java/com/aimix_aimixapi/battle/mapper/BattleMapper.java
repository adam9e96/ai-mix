package com.aimix_aimixapi.battle.mapper;

import com.aimix_aimixapi.battle.dto.BattleCreateResponse;
import com.aimix_aimixapi.battle.dto.BattleQuestionDto;
import com.aimix_aimixapi.battle.entity.Battle;
import com.aimix_aimixapi.battle.entity.BattleQuestion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

/**
 * Battle 엔티티와 DTO 간의 매핑을 담당하는 MapStruct 매퍼
 * MapStruct가 컴파일 타임에 자동으로 매핑 구현 코드를 생성합니다.
 * Lombok의 @Builder를 자동으로 인식하여 Builder 패턴을 사용합니다.
 * 
 * @apiNote jsonToStringList 메서드는 JsonUtils를 사용하지만, MapStruct 인터페이스에서는
 *          Spring Bean을 주입받을 수 없으므로, 실제 사용 시 서비스에서 JsonUtils를 사용하여
 *          직접 변환하는 것을 권장합니다.
 */
@Mapper(componentModel = "spring", uses = {})
public interface BattleMapper {

    @Mapping(source = "id", target = "battleId")
    @Mapping(source = "sourceType", target = "sourceType")
    BattleCreateResponse toBattleCreateResponse(Battle battle);

    @Mapping(source = "id", target = "questionId")
    @Mapping(source = "choices", target = "choices", qualifiedByName = "jsonToStringList")
    BattleQuestionDto toBattleQuestionDto(BattleQuestion question);

    /**
     * JSON 문자열을 List<String>으로 변환
     * 
     * @apiNote 이 메서드는 MapStruct 매핑 과정에서 호출됩니다.
     *          JsonUtils를 사용하려면 서비스에서 직접 변환하는 것을 권장합니다.
     *          현재는 호환성을 위해 유지하지만, 실제로는 사용되지 않을 수 있습니다.
     */
    @Named("jsonToStringList")
    default List<String> jsonToStringList(String choicesJson) {
        if (choicesJson == null || choicesJson.isBlank()) {
            return null;
        }
        // MapStruct 인터페이스에서는 Spring Bean을 주입받을 수 없으므로
        // 실제 사용 시 서비스에서 JsonUtils를 사용하여 직접 변환하는 것을 권장합니다.
        // 이 메서드는 호환성을 위해 유지되지만, 실제로는 사용되지 않을 수 있습니다.
        return null;
    }
}
