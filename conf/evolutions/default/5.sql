-- !Ups

-- -----------------------------------------------------
-- Table `mydb`.`post`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `internship_project`.`post` (
    `id` INT NOT NULL,
    `title` VARCHAR(45) NULL,
    `content` VARCHAR(500) NULL,
    `date_time` DATETIME NULL,
    `owner_id` INT NOT NULL,
    PRIMARY KEY (`id`),
    INDEX `fk_post_user1_idx` (`owner_id` ASC) VISIBLE,
    CONSTRAINT `fk_post_user1`
        FOREIGN KEY (`owner_id`)
            REFERENCES `internship_project`.`user` (`id`)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION)
    ENGINE = InnoDB;

INSERT INTO `internship_project`.`post` (`id`, `owner_id`, `title`, `content`, `date_time`) VALUES
('1', '1', 'Lorem ipsum', 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut vel est tellus. ', '2022-04-02 10:36:31');
INSERT INTO `internship_project`.`post` (`id`, `owner_id`, `title`, `content`, `date_time`) VALUES
('2', '1', 'Nulla vitae', 'Nulla vitae eleifend dolor, hendrerit consequat purus. ', '2022-03-26 10:42:11');

INSERT INTO `internship_project`.`post` (`id`, `owner_id`, `title`, `content`, `date_time`) VALUES
('3', '2', 'Morbi vestibulum', 'Morbi vestibulum metus feugiat libero fringilla posuere. ', '2022-02-16 09:36:31');

INSERT INTO `internship_project`.`post` (`id`, `owner_id`, `title`, `content`, `date_time`) VALUES
('4', '3', 'Mauris vel', 'Mauris vel iaculis justo. ', '2022-01-06 12:22:11');
INSERT INTO `internship_project`.`post` (`id`, `owner_id`, `title`, `content`, `date_time`) VALUES
('5', '3', 'Nullam turpis', 'Nullam turpis turpis, vehicula quis leo ac, faucibus dignissim quam. ', '2022-02-06 15:07:11');
INSERT INTO `internship_project`.`post` (`id`, `owner_id`, `title`, `content`, `date_time`) VALUES
('6', '3', 'Nunc aliquet', 'Nunc aliquet vitae quam quis finibus. ', '2022-04-01 10:52:11');

-- !Downs

DROP TABLE `internship_project`.`post`;