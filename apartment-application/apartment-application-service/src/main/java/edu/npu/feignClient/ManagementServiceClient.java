package edu.npu.feignClient;

import edu.npu.entity.Apartment;
import edu.npu.entity.Bed;
import edu.npu.entity.Department;
import edu.npu.entity.Room;
import edu.npu.feignClient.fallback.ManagementServiceClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "management-api",
        path = "/management/remote",
        fallbackFactory = ManagementServiceClientFallbackFactory.class)
public interface ManagementServiceClient {

    @GetMapping("/department")
    Department getDepartmentById(@RequestParam(value = "id") Long id);

    @GetMapping("/room")
    Room getRoomById(@RequestParam(value = "id") Long id);

    @GetMapping("/bed")
    Bed getBedById(@RequestParam(value = "id") Long id);

    @PutMapping("/bed")
    boolean updateBed(@RequestBody Bed bed);

    @GetMapping("/apartment")
    Apartment getApartmentByAdminId(@RequestParam(value = "adminId") Long adminId);

    @GetMapping("/bedsByApartment")
    List<Bed> getBedsByApartmentId(
            @RequestParam(value = "apartmentId") Long apartmentId
    );
}
