package com.streammastery.model;

/**
 * A lightweight DTO for transferring employee data.
 * Used in map/transformation exercises.
 */
public class EmployeeDTO {

    private final Integer id;
    private final String fullName;
    private final String department;
    private final Double salary;

    public EmployeeDTO(Integer id, String fullName, String department, Double salary) {
        this.id = id;
        this.fullName = fullName;
        this.department = department;
        this.salary = salary;
    }

    public Integer getId() { return id; }
    public String getFullName() { return fullName; }
    public String getDepartment() { return department; }
    public Double getSalary() { return salary; }

    public static EmployeeDTO fromEmployee(Employee e) {
        return new EmployeeDTO(e.getId(), e.getFullName(), e.getDepartment(), e.getSalary());
    }

    @Override
    public String toString() {
        return "EmployeeDTO{id=" + id + ", fullName='" + fullName +
               "', department='" + department + "', salary=" + salary + "}";
    }
}
