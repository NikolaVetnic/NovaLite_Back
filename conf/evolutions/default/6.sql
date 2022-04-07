-- !Ups

-- -----------------------------------------------------
-- Table `internship_project`.`post_reaction`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `internship_project`.`post_reaction` (
    `user_id` INT NOT NULL,
    `post_id` INT NOT NULL,
    `reaction_id` INT NOT NULL,
    PRIMARY KEY (`user_id`, `post_id`),
    INDEX `fk_user_has_post_post1_idx` (`post_id` ASC) VISIBLE,
    INDEX `fk_user_has_post_user1_idx` (`user_id` ASC) VISIBLE,
    INDEX `fk_post_reaction_reaction1_idx` (`reaction_id` ASC) VISIBLE,
    CONSTRAINT `fk_user_has_post_user1`
        FOREIGN KEY (`user_id`)
            REFERENCES `internship_project`.`user` (`id`)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION,
    CONSTRAINT `fk_user_has_post_post1`
        FOREIGN KEY (`post_id`)
            REFERENCES `internship_project`.`post` (`id`)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION,
    CONSTRAINT `fk_post_reaction_reaction1`
        FOREIGN KEY (`reaction_id`)
            REFERENCES `internship_project`.`reaction` (`id`)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION)
    ENGINE = InnoDB;

INSERT INTO `internship_project`.`post_reaction` (`user_id`, `post_id`, `reaction_id`) VALUES ('2', '1', '1');
INSERT INTO `internship_project`.`post_reaction` (`user_id`, `post_id`, `reaction_id`) VALUES ('2', '6', '2');

-- !Downs

DROP TABLE `internship_project`.`post_reaction`;