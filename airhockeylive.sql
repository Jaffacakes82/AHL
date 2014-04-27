-- phpMyAdmin SQL Dump
-- version 4.0.9
-- http://www.phpmyadmin.net
--
-- Host: 127.0.0.1
-- Generation Time: Apr 08, 2014 at 07:04 PM
-- Server version: 5.5.34
-- PHP Version: 5.4.22

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `airhockeylive`
--

DELIMITER $$
--
-- Procedures
--
CREATE DEFINER=`root`@`localhost` PROCEDURE `GetPlayerInfo`(gameId char(38))
BEGIN
	DROP TEMPORARY TABLE IF EXISTS `TempTable`;

    CREATE TEMPORARY TABLE TempTable 
    (
        ID char(38), 
        Username varchar(255), 
        Twitter varchar(255)
    ); 
    
    INSERT INTO TempTable (ID, Username, Twitter) 
    SELECT player.ID, player.Username, player.Twitter 
    FROM game LEFT JOIN player ON player.ID = game.Player1 
    WHERE game.ID = gameId; 
    
    INSERT INTO TempTable (ID, Username, Twitter) 
    SELECT player.ID, player.Username, player.Twitter 
    FROM game LEFT JOIN player ON player.ID = game.Player2 
    WHERE game.ID = gameId; 
    
    SELECT * from TempTable;
END$$

DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `game`
--

CREATE TABLE IF NOT EXISTS `game` (
  `ID` char(38) NOT NULL,
  `Player1` char(38) NOT NULL,
  `Player2` char(38) DEFAULT NULL,
  `State` varchar(255) NOT NULL,
  `Winner` char(38) DEFAULT NULL,
  `Player1Score` int(11) DEFAULT NULL,
  `Player2Score` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `ID_UNIQUE` (`ID`),
  KEY `FKPlayer1_idx` (`Player1`),
  KEY `FKPlayer2_idx` (`Player2`),
  KEY `FKWinner_idx` (`Winner`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `game`
--

INSERT INTO `game` (`ID`, `Player1`, `Player2`, `State`, `Winner`, `Player1Score`, `Player2Score`) VALUES
('1c0b863a-4645-404a-a2c2-c7da32e323fc', '44679d51-e5e4-4a86-89c1-ffb565da9c8f', '2c746138-ddce-41e5-bbb3-ed227c274508', 'STARTED', NULL, 0, 0),
('3ced49c4-eb37-45f5-9399-f12a23d3b12a', '44679d51-e5e4-4a86-89c1-ffb565da9c8f', '2c746138-ddce-41e5-bbb3-ed227c274508', 'FINISHED', '44679d51-e5e4-4a86-89c1-ffb565da9c8f', 7, 0);

-- --------------------------------------------------------

--
-- Table structure for table `player`
--

CREATE TABLE IF NOT EXISTS `player` (
  `ID` char(38) NOT NULL,
  `Username` varchar(255) NOT NULL,
  `Password` varchar(255) NOT NULL,
  `Name` varchar(255) DEFAULT NULL,
  `Twitter` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `ID_UNIQUE` (`ID`),
  UNIQUE KEY `Username_UNIQUE` (`Username`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `player`
--

INSERT INTO `player` (`ID`, `Username`, `Password`, `Name`, `Twitter`) VALUES
('2c746138-ddce-41e5-bbb3-ed227c274508', 'jellis', 'legolas', 'Joseph Ellis', '@Jaffacakes82'),
('3731a63a-8b06-476f-a7fd-e58529dd4cec', 'tes', 'test', '', '@'),
('44679d51-e5e4-4a86-89c1-ffb565da9c8f', 'Bob', 'real', 'Not Real', '@');

--
-- Constraints for dumped tables
--

--
-- Constraints for table `game`
--
ALTER TABLE `game`
  ADD CONSTRAINT `FKPlayer1` FOREIGN KEY (`Player1`) REFERENCES `player` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  ADD CONSTRAINT `FKPlayer2` FOREIGN KEY (`Player2`) REFERENCES `player` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  ADD CONSTRAINT `FKWinner` FOREIGN KEY (`Winner`) REFERENCES `player` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
