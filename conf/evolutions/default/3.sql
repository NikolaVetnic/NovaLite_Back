-- !Ups

-- -----------------------------------------------------
-- Table `internship_project`.`befriends`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `internship_project`.`befriends` (
    `user_id0` INT NOT NULL,
    `user_id1` INT NOT NULL,
    `status` INT NOT NULL,
    PRIMARY KEY (`user_id0`, `user_id1`),
    INDEX `fk_user_has_user_user2_idx` (`user_id1` ASC) VISIBLE,
    INDEX `fk_user_has_user_user1_idx` (`user_id0` ASC) VISIBLE,
    CONSTRAINT `fk_user_has_user_user1`
        FOREIGN KEY (`user_id0`)
            REFERENCES `internship_project`.`user` (`id`)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION,
    CONSTRAINT `fk_user_has_user_user2`
        FOREIGN KEY (`user_id1`)
            REFERENCES `internship_project`.`user` (`id`)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION)
    ENGINE = InnoDB;

INSERT INTO `internship_project`.`befriends` (`user_id0`, `user_id1`, `status`) VALUES ('1', '2', '2');
INSERT INTO `internship_project`.`befriends` (`user_id0`, `user_id1`, `status`) VALUES ('2', '3', '2');
INSERT INTO `internship_project`.`befriends` (`user_id0`, `user_id1`, `status`) VALUES ('1', '3', '1');

-- !Downs

DROP TABLE `internship_project`.`befriends`;