package com.webapp.intelligentworkspace.model.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false)
    private String file_name;

    @Column(nullable = false)
    private String file_url;

    @Column(nullable = false)
    private float size;


    @ManyToOne
    @JoinColumn(name = "storage_id")
    @JsonManagedReference
    @JsonIgnore
    private Storage storage;



    @ManyToOne
    @JoinColumn(name = "folder_id", referencedColumnName = "id")
    @JsonManagedReference
    @JsonIgnore
    private Folder folder;

   @Column(name = "isDeleted")
    private Boolean isDeleted;
}
