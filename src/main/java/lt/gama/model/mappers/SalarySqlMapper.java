package lt.gama.model.mappers;

import lt.gama.model.dto.documents.SalaryDto;
import lt.gama.model.dto.entities.GLOperationDto;
import lt.gama.model.sql.documents.SalarySql;
import lt.gama.model.type.doc.DocCharge;
import lt.gama.model.type.doc.DocChargeAmount;
import lt.gama.model.type.doc.DocPosition;
import lt.gama.model.type.salary.WorkData;
import lt.gama.model.type.salary.WorkHoursDay;
import lt.gama.model.type.salary.WorkHoursPosition;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(uses = {UtilsMapper.class}, componentModel = "spring")
public interface SalarySqlMapper extends IBaseMapper<SalaryDto, SalarySql> {

    @Override
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "employee", ignore = true)
    SalaryDto toDto(SalarySql entity);

    @Override
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "employee", ignore = true)
    SalarySql toEntity(SalaryDto dto);

    List<GLOperationDto> cloneListGLOperationDto(List<GLOperationDto> entity);

    @Mapping(target = "sum", ignore = true)
    GLOperationDto cloneGLOperationDto(GLOperationDto entity);

    List<DocChargeAmount> cloneListDocChargeAmount(List<DocChargeAmount> src);

    DocChargeAmount cloneDocChargeAmount(DocChargeAmount src);

    DocCharge cloneDocCharge(DocCharge src);

    WorkHoursPosition cloneWorkHoursPosition(WorkHoursPosition src);

    DocPosition cloneDocPosition(DocPosition src);

    List<DocPosition> cloneListDocPosition(List<DocPosition> dto);

    WorkHoursDay cloneWorkHoursDay(WorkHoursDay src);

    List<WorkHoursDay> cloneListWorkHoursDay(List<WorkHoursDay> src);

    WorkData cloneWorkData(WorkData src);

}
