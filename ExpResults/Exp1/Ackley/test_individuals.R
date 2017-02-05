   file_test    = "test.out"
   file_result  = file("temp_result",open="w");
   con          = file(file_test, open="r")
   line         = readLines(con)
   for (i in 1:length(line)) {
      if (i%%3 == 0)
         writeLines(line[i], file_result)
   }
   close(file_result)    
