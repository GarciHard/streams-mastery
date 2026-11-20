package com.streammastery.model;

import java.util.Objects;

/**
 * Represents an employee in a tech company.
 * Used across multiple lessons for stream operations.
 */
public class Employee {

    private final Integer id;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String department;
    private final String title;
    private final Integer age;
    private final Double salary;
    private final String city;
    private final String gender;
    private final Integer yearsOfExperience;
    private final String programmingLanguage;

    public Employee(Integer id, String firstName, String lastName, String email,
                    String department, String title, Integer age, Double salary,
                    String city, String gender, Integer yearsOfExperience,
                    String programmingLanguage) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.department = department;
        this.title = title;
        this.age = age;
        this.salary = salary;
        this.city = city;
        this.gender = gender;
        this.yearsOfExperience = yearsOfExperience;
        this.programmingLanguage = programmingLanguage;
    }

    public Integer getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getDepartment() { return department; }
    public String getTitle() { return title; }
    public Integer getAge() { return age; }
    public Double getSalary() { return salary; }
    public String getCity() { return city; }
    public String getGender() { return gender; }
    public Integer getYearsOfExperience() { return yearsOfExperience; }
    public String getProgrammingLanguage() { return programmingLanguage; }

    public String getFullName() { return firstName + " " + lastName; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return Objects.equals(id, employee.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Employee{id=" + id + ", name='" + firstName + " " + lastName +
               "', dept='" + department + "', title='" + title +
               "', age=" + age + ", salary=" + salary +
               ", city='" + city + "', lang='" + programmingLanguage + "'}";
    }
}
