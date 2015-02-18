################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CXX_SRCS += \
../src/lpaggreg_wrap.cxx 

OBJS += \
./src/lpaggreg_wrap.o 

CXX_DEPS += \
./src/lpaggreg_wrap.d 


# Each subdirectory must supply rules for building sources it contributes
src/%.o: ../src/%.cxx
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -m32 -I/usr/java/latest/include -I/usr/java/latest/include/linux -O1 -Wall -c -fmessage-length=0 -fPIC -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


