package com.hotelbooking.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "hotels")
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "h_id")
    private Long id;

    @Column(nullable = false)
    private String hotelName;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false, length = 1000)
    private String description;

    private String mapsURL;

    @ManyToOne
    @JoinColumn(name = "manager_id", nullable = false)
    private User manager;
}
