package com.hotelbooking.service.impl;

import com.hotelbooking.dto.RoomDTO;
import com.hotelbooking.entity.Hotel;
import com.hotelbooking.entity.Room;
import com.hotelbooking.enums.RoomType;
import com.hotelbooking.mapper.BookingMapper;
import com.hotelbooking.mapper.RoomMapper;
import com.hotelbooking.repository.BookingRepository;
import com.hotelbooking.repository.HotelRepository;
import com.hotelbooking.repository.RoomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HotelManagerServiceImplTest {

    @Mock
    private HotelRepository hotelRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private RoomMapper roomMapper;
    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private HotelManagerServiceImpl hotelManagerService;

    @Test
    void addRoomShouldSetAvailabilityFromTotalRooms() {
        Hotel hotel = Hotel.builder().id(10L).build();
        RoomDTO roomDTO = RoomDTO.builder()
                .roomType(RoomType.DELUXE)
                .pricePerNight(200.0)
                .totalRooms(0)
                .build();
        Room mapped = Room.builder().hotel(hotel).build();

        when(hotelRepository.findByManager_Id(3L)).thenReturn(Optional.of(hotel));
        when(roomMapper.toEntity(roomDTO, hotel)).thenReturn(mapped);

        hotelManagerService.addRoom(roomDTO, 3L);

        assertEquals(false, roomDTO.isAvailableRooms());
        verify(roomRepository).save(mapped);
    }

    @Test
    void updateRoomShouldFailWhenRoomBelongsToAnotherHotel() {
        RoomDTO roomDTO = RoomDTO.builder()
                .r_id(9L)
                .roomType(RoomType.SUITE)
                .pricePerNight(500.0)
                .totalRooms(2)
                .build();
        Hotel managerHotel = Hotel.builder().id(1L).build();
        Room room = Room.builder().id(9L).hotel(Hotel.builder().id(2L).build()).build();

        when(hotelRepository.findByManager_Id(6L)).thenReturn(Optional.of(managerHotel));
        when(roomRepository.findById(9L)).thenReturn(Optional.of(room));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> hotelManagerService.updateRoom(roomDTO, 6L));

        assertEquals("You cannot update room from another hotel", ex.getMessage());
    }

    @Test
    void updateRoomShouldPersistMappedValuesAndAvailability() {
        RoomDTO roomDTO = RoomDTO.builder()
                .r_id(9L)
                .roomType(RoomType.DOUBLE)
                .pricePerNight(350.0)
                .totalRooms(4)
                .build();
        Hotel managerHotel = Hotel.builder().id(2L).build();
        Room room = Room.builder().id(9L).hotel(managerHotel).roomType(RoomType.SINGLE).totalRooms(1).build();

        when(hotelRepository.findByManager_Id(6L)).thenReturn(Optional.of(managerHotel));
        when(roomRepository.findById(9L)).thenReturn(Optional.of(room));

        hotelManagerService.updateRoom(roomDTO, 6L);

        ArgumentCaptor<Room> captor = ArgumentCaptor.forClass(Room.class);
        verify(roomRepository).save(captor.capture());
        assertEquals(RoomType.DOUBLE, captor.getValue().getRoomType());
        assertEquals(4, captor.getValue().getTotalRooms());
        assertEquals(true, captor.getValue().isAvailableRooms());
    }
}
