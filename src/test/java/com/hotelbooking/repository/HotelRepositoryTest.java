package com.hotelbooking.repository;

import com.hotelbooking.entity.Hotel;
import com.hotelbooking.entity.User;
import com.hotelbooking.enums.Role;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class HotelRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private HotelRepository hotelRepository;

    @Test
    void findByCityIgnoreCaseShouldMatchIgnoringCase() {
        User manager = persistManager("manager1@test.com");
        Hotel hotel = Hotel.builder()
                .hotelName("Sunrise")
                .city("Pune")
                .description("Good hotel description")
                .mapsURL("maps")
                .manager(manager)
                .build();
        entityManager.persist(hotel);
        entityManager.flush();

        List<Hotel> results = hotelRepository.findByCityIgnoreCase("pUnE");

        assertEquals(1, results.size());
        assertEquals("Sunrise", results.getFirst().getHotelName());
    }

    @Test
    void findTop4ByOrderByIdDescShouldReturnFourNewestHotels() {
        User manager = persistManager("manager2@test.com");
        for (int i = 1; i <= 5; i++) {
            entityManager.persist(Hotel.builder()
                    .hotelName("Hotel " + i)
                    .city("City")
                    .description("Description " + i + " ok")
                    .mapsURL("maps-" + i)
                    .manager(manager)
                    .build());
        }
        entityManager.flush();

        List<Hotel> hotels = hotelRepository.findTop4ByOrderByIdDesc();

        assertEquals(4, hotels.size());
        assertTrue(hotels.get(0).getId() > hotels.get(1).getId());
    }

    private User persistManager(String email) {
        User manager = User.builder()
                .email(email)
                .password("pass")
                .fullName("Manager")
                .role(Role.ROLE_HOTEL_MANAGER)
                .enabled(true)
                .build();
        entityManager.persist(manager);
        return manager;
    }
}
