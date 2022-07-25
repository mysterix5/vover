package com.github.mysterix5.vover.model.word;

import com.github.mysterix5.vover.model.other.Accessibility;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordManagementDTO {
    private String id;
    private String word;
    private String tag;
    private Accessibility accessibility;

    public RecordManagementDTO(WordDbEntity wordDb){
        id = wordDb.getId();
        word = wordDb.getWord();
        tag = wordDb.getTag();
        accessibility = wordDb.getAccessibility();
    }
}