package it.finanze.sanita.fse2.ms.srvquery.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CodeSystemsResDTO {

    private List<String> codeSystems;
}
