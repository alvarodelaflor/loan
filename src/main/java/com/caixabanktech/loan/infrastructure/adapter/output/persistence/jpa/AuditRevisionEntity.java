package com.caixabanktech.loan.infrastructure.adapter.output.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

@Entity
@Data
@Table(name = "REVINFO")
@RevisionEntity
public class AuditRevisionEntity {

    @Id
    @Column(name = "REV")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rev_seq")
    @SequenceGenerator(name = "rev_seq", sequenceName = "REVINFO_SEQ")
    @RevisionNumber
    private int id;

    @Column(name = "REVTSTMP")
    @RevisionTimestamp
    private long timestamp;
}