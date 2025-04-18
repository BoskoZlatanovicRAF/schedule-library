# Scheduler - library

A flexible Java-based scheduling system for managing room bookings and meetings with multiple implementation approaches.

## About the Project

This project provides a comprehensive solution for managing meeting schedules in different rooms. It implements a modular architecture with a well-defined API specification that allows for different scheduling implementations. The system can handle both collection-based scheduling (storing specific meetings at exact dates and times) and weekly recurring schedules over a specified period.

## Key Features

- **Multiple Scheduling Approaches**:
  - Collection-based scheduling for one-time meetings
  - Weekly recurring scheduling for regular meetings

- **Comprehensive Meeting Management**:
  - Add, remove, and reschedule meetings
  - Check for overlapping meetings to prevent double-booking
  - Associate additional attributes with meetings (subjects, professors, etc.)

- **Advanced Filtering Capabilities**:
  - Filter meetings by exact date
  - Filter by date range and time period
  - Filter by room attributes
  - Find available time gaps for scheduling new meetings

- **Import/Export Functionality**:
  - Import schedules from CSV and JSON files
  - Export schedules to CSV, JSON, and PDF formats
  - Configurable mapping of data fields

- **Room Management**:
  - Track rooms and their features
  - Find available rooms during specific time slots

- **User Interface**:
  - Command-line interface for managing schedules
  - Interactive menu for all operations

## Technologies Used

- **Java 18**: Core programming language
- **Maven**: Dependency management and build tool
- **Project Lombok**: Reduces boilerplate code with annotations
- **Apache Commons CSV**: For CSV file parsing and generation
- **Google Gson**: For JSON processing
- **Apache PDFBox**: For PDF document generation
- **Java Streams API**: For efficient data processing
- **Java Time API**: For date and time handling

## Project Structure

The project is organized into three main modules:

1. **api-specification**: Defines the core interfaces and models for the scheduling system
2. **Implementation1 - collection-schedule**: Implements scheduling as a collection of specific meetings
3. **Implementation2 - weekly-schedule**: Implements scheduling on a weekly recurring basis
4. **test-app**: Provides a command-line interface to test both implementations

This modular approach allows for easy extension with new scheduling implementations while maintaining a consistent API.
