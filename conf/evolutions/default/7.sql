-- !Ups

-- -----------------------------------------------------
-- Table `mydb`.`comment`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `internship_project`.`comment` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `content` VARCHAR(200) NULL,
    `owner_id` INT NOT NULL,
    `post_id` INT NOT NULL,
    `date_time` DATETIME NULL,
    PRIMARY KEY (`id`),
    INDEX `fk_comment_user1_idx` (`owner_id` ASC) VISIBLE,
    INDEX `fk_comment_post1_idx` (`post_id` ASC) VISIBLE,
    CONSTRAINT `fk_comment_user1`
        FOREIGN KEY (`owner_id`)
            REFERENCES `internship_project`.`user` (`id`)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION,
    CONSTRAINT `fk_comment_post1`
        FOREIGN KEY (`post_id`)
            REFERENCES `internship_project`.`post` (`id`)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION)
    ENGINE = InnoDB;

# INSERT INTO `internship_project`.`post_reaction` (`user_id`, `post_id`, `reaction_id`) VALUES ('2', '1', '1');
# INSERT INTO `internship_project`.`post_reaction` (`user_id`, `post_id`, `reaction_id`) VALUES ('2', '6', '6');

INSERT INTO `internship_project`.`comment` (`id`, `post_id`, `owner_id`, `content`, `date_time`) VALUES
    ('1', '3', '1', 'Mauris quis tincidunt lorem.', '2022-04-02 10:36:33');
INSERT INTO `internship_project`.`comment` (`id`, `post_id`, `owner_id`, `content`, `date_time`) VALUES
    ('2', '4', '2', 'Curabitur tincidunt diam sed molestie dictum.', '2022-03-26 10:44:11');
INSERT INTO `internship_project`.`comment` (`id`, `post_id`, `owner_id`, `content`, `date_time`) VALUES
    ('3', '3', '3', 'Aenean convallis, orci non aliquam lobortis, enim mauris fringilla arcu, in feugiat augue ligula vitae augue.', '2022-02-16 09:38:31');


-- !Downs

DROP TABLE `internship_project`.`comment`;