-- !Ups

-- -----------------------------------------------------
-- Table `internship_project`.`comment_reaction`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `internship_project`.`comment_reaction` (
    `comment_id` INT NOT NULL,
    `user_id` INT NOT NULL,
    `reaction_id` INT NOT NULL,
    PRIMARY KEY (`comment_id`, `user_id`),
    INDEX `fk_comment_has_user_user1_idx` (`user_id` ASC) VISIBLE,
    INDEX `fk_comment_has_user_comment1_idx` (`comment_id` ASC) VISIBLE,
    INDEX `fk_comment_has_user_reaction1_idx` (`reaction_id` ASC) VISIBLE,
    CONSTRAINT `fk_comment_has_user_comment1`
        FOREIGN KEY (`comment_id`)
            REFERENCES `internship_project`.`comment` (`id`)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION,
    CONSTRAINT `fk_comment_has_user_user1`
        FOREIGN KEY (`user_id`)
            REFERENCES `internship_project`.`user` (`id`)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION,
    CONSTRAINT `fk_comment_has_user_reaction1`
        FOREIGN KEY (`reaction_id`)
            REFERENCES `internship_project`.`reaction` (`id`)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION)
    ENGINE = InnoDB;

INSERT INTO `internship_project`.`comment_reaction` (`user_id`, `comment_id`, `reaction_id`) VALUES ('2', '1', '1');
INSERT INTO `internship_project`.`comment_reaction` (`user_id`, `comment_id`, `reaction_id`) VALUES ('2', '3', '2');

-- !Downs

DROP TABLE `internship_project`.`comment_reaction`;