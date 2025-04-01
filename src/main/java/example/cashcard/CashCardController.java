package example.cashcard;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/cashcards")
public class CashCardController {
    private final CashCardRepository cashCardRepository;

    public CashCardController(CashCardRepository cashCardRepository) {
        this.cashCardRepository = cashCardRepository;
    }

    @GetMapping("/{requestId}")
    private ResponseEntity<CashCard> findById(@PathVariable Long requestId) {
        return ResponseEntity.of(cashCardRepository.findById(requestId));
        // or below - the same thing but more verbose, or when you just wanna show off lol:
        // return cashCardRepository.findById(requestId)
        //         .map(ResponseEntity::ok)
        //         .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    private ResponseEntity<Void> createNewCard(@RequestBody CashCard cashCardRequest, UriComponentsBuilder ucb) {
        CashCard savedCashCard = cashCardRepository.save(cashCardRequest);
        URI locationOfNewCashCard = ucb
                .path("cashcards/{id}")
                .buildAndExpand(savedCashCard.id())
                .toUri();
        return ResponseEntity.created(locationOfNewCashCard).build();
    }

    @GetMapping
    private ResponseEntity<Iterable<CashCard>> findAll(Pageable pageable) {
        Page<CashCard> page = cashCardRepository.findAll(
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount"))
                        ));
        return ResponseEntity.ok(page.getContent());
    }
}
