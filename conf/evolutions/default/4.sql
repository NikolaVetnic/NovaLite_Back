-- !Ups

-- -----------------------------------------------------
-- Table `internship_project`.`requests`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `internship_project`.`requests` (
    `user_id0` INT NOT NULL,
    `user_id1` INT NOT NULL,
    PRIMARY KEY (`user_id0`, `user_id1`),
    INDEX `fk_user_has_user_user4_idx` (`user_id1` ASC) VISIBLE,
    INDEX `fk_user_has_user_user3_idx` (`user_id0` ASC) VISIBLE,
    CONSTRAINT `fk_user_has_user_user3`
        FOREIGN KEY (`user_id0`)
            REFERENCES `internship_project`.`user` (`id`)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION,
    CONSTRAINT `fk_user_has_user_user4`
        FOREIGN KEY (`user_id1`)
            REFERENCES `internship_project`.`user` (`id`)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION)
    ENGINE = InnoDB;

INSERT INTO `internship_project`.`requests` (`user_id0`, `user_id1`) VALUES ('1', '3');

-- !Downs

DROP TABLE `internship_project`.`requests`;