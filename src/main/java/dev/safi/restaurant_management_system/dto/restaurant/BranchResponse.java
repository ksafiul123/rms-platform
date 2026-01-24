package dev.safi.restaurant_management_system.dto.restaurant;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Branch Response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Restaurant branch details")
public class BranchResponse {

    private Long id;
    private String branchCode;
    private String branchName;
    private String contactEmail;
    private String contactPhone;
    private String address;
    private String city;
    private String zipCode;
    private String state;
    private String country;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Boolean isMainBranch;
    private String openingTime;
    private String closingTime;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
