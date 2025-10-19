package com.example.emailtojira.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailDetail {
        private String id;       // ID duy nhất của email
        private String tieuDe;
        private String noiDung;
        private String nguoiGui;

}
