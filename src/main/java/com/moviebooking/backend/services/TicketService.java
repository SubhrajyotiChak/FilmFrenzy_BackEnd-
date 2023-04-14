package com.moviebooking.backend.services;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.moviebooking.backend.model.CustomizedUser;
import com.moviebooking.backend.model.Movie;
import com.moviebooking.backend.model.Ticket;
import com.moviebooking.backend.repository.MovieRepository;
import com.moviebooking.backend.repository.TicketRepository;
import com.moviebooking.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private UserRepository userRepository;

    public Map<String, List<Ticket>> getTicketByMovieId(@RequestParam Long movieId, Long timeId) {

        Movie movie = movieRepository.findById(movieId).orElse(new Movie());
        List<Ticket> ticketList = movie.getTickets();
        if (timeId == 0) {
            ticketList.subList(30, 60).clear();
        } else {
            ticketList.subList(0, 30).clear();
        }
        List<Ticket> Booked = new ArrayList<>(), Available = new ArrayList<>();
        for (int i = 0; i < ticketList.size(); i++) {
            if (ticketList.get(i).isBookedOrNot()) {
                Booked.add(ticketList.get(i));
            } else {
                Available.add(ticketList.get(i));
            }
        }
        return Map.of("Total", ticketList, "Booked", Booked, "Available", Available);
    }

    public Map<String, List<Ticket>> getSeats(@PathVariable("movieIdStr") String movieIdStr, @PathVariable("ticketIdStr") String ticketIdStr) {
        Long movieId = Long.parseLong(movieIdStr);
        Long timeId = Long.parseLong(ticketIdStr);
        return getTicketByMovieId(movieId, timeId);
    }

    public Map<String, String> getMovieInfo(@PathVariable("movieIdStr") String movieIdStr, @PathVariable("ticketIdStr") String ticketIdStr) {
        long movieId = Long.parseLong(movieIdStr);
        long timeId = Long.parseLong(ticketIdStr);
        Optional<Movie> movieQuery = movieRepository.findMovieById(movieId);
        String movieName = "", movieTime = "";
        if (movieQuery.isPresent()) {
            Movie movie = movieQuery.get();
            movieName = movie.getName();
            List<Ticket> ticketList = movie.getTickets();
            movieTime = ticketList.get((int) timeId == 0 ? 0 : 30).getTicketTime();
        }
        return Map.of("movieName", movieName, "movieTime", movieTime);
    }






    /**




It then iterates through the list of Ticket objects and retrieves the corresponding Ticket object from the database using the ticketRepository.

If the Ticket object is present, it updates the booked status to true and sets the user object and seat information for the ticket.

It then saves the updated ticket object back to the database using the ticketRepository.

If the Ticket object is not present, it returns a BAD_REQUEST response.

If the user object is not present, it returns a BAD_REQUEST response.

If the process is successful, it returns an OK response.

*/

// It takes in the movieIdStr and userIdStr as path variables and reservedTickets as the request body in the form of a JSON string.

// The reserveSeats method is used to update the booking status of a ticket for a particular movie by a particular user.
    public ResponseEntity<?> reserveSeats(@PathVariable("movieIdStr") String movieIdStr, @PathVariable("userid") String userIdStr, @RequestBody String reservedTickets) {

// It first parses the movieIdStr and userIdStr into Long values. It then retrieves the user object associated with the userId from the database using the userRepository.
        Long movieId = Long.parseLong(movieIdStr);
        Long userId = Long.parseLong(userIdStr);

        Optional<CustomizedUser> customizedUser = userRepository.findById(userId);
//If the user object is present, it uses the Gson library to parse the reservedTickets JSON string into a list of Ticket objects.
        if (customizedUser.isPresent()) {
            Gson gson = new Gson();
            Type typeMovie = new TypeToken<List<Ticket>>() {
            }.getType();
            List<Ticket> ticketList = gson.fromJson(reservedTickets, typeMovie);
            for (Ticket ticketInfo : ticketList) {
                Optional<Ticket> optionalTicket = ticketRepository.findTicketById(ticketInfo.getId());
                if (optionalTicket.isPresent()) {
                    Ticket currTicket = optionalTicket.get();
                    currTicket.setBookedOrNot(true);
                    currTicket.setCustomizedUser(customizedUser.get());
                    currTicket.setSeatInfo(ticketInfo.getSeatInfo());
                    ticketRepository.save(currTicket);
                } else {
                    return new ResponseEntity<>("User Not Found", HttpStatus.BAD_REQUEST);
                }
            }
            return new ResponseEntity<>("Update Success", HttpStatus.OK);
        }
        return new ResponseEntity<>("User Not Found", HttpStatus.BAD_REQUEST);
    }
}
