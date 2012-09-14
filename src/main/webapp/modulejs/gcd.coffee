gcd = (a, b) -> if (b == 0) then a else gcd(b, a % b)
$("#button").click ->
  a = $("#a").val()
  b = $("#b").val()
  $("#c").html gcd(a, b)

