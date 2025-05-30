package by.kapinskiy.Publisher.services;


import by.kapinskiy.Publisher.DTOs.Requests.IssueRequestDTO;
import by.kapinskiy.Publisher.DTOs.Responses.IssueResponseDTO;
import by.kapinskiy.Publisher.models.Issue;
import by.kapinskiy.Publisher.models.Tag;
import by.kapinskiy.Publisher.models.User;
import by.kapinskiy.Publisher.repositories.IssuesRepository;
import by.kapinskiy.Publisher.repositories.TagsRepository;
import by.kapinskiy.Publisher.repositories.UsersRepository;
import by.kapinskiy.Publisher.utils.exceptions.NotFoundException;
import by.kapinskiy.Publisher.utils.mappers.IssuesMapper;
import by.kapinskiy.Publisher.utils.mappers.TagsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class IssuesService {
    private final IssuesRepository issuesRepository;
    private final UsersRepository usersRepository;
    private final IssuesMapper issuesMapper;
    private final TagsMapper tagsMapper;
    private TagsRepository tagsRepository;

    @Autowired
    public IssuesService(IssuesRepository issuesRepository, UsersRepository usersRepository,
                         IssuesMapper issuesMapper, TagsMapper tagsMapper, TagsRepository tagsRepository) {
        this.issuesRepository = issuesRepository;
        this.usersRepository = usersRepository;
        this.issuesMapper = issuesMapper;
        this.tagsMapper = tagsMapper;
        this.tagsRepository = tagsRepository;
    }

    private void setUser(Issue issue, long userId){
        User user = usersRepository.findById(userId).orElseThrow(() -> new NotFoundException("User with such id does not exist"));
        issue.setUser(user);
    }

    @Transactional
    public IssueResponseDTO save(IssueRequestDTO issueRequestDTO) {
        Issue issue = issuesMapper.toIssue(issueRequestDTO);
        setUser(issue, issueRequestDTO.getUserId());
        if (issueRequestDTO.getTags().size() > 0)
            saveTags(issue, issueRequestDTO.getTags().stream().map(Tag::new).toList());
        issue.setCreated(new Date());
        issue.setModified(new Date());
        return issuesMapper.toIssueResponse(issuesRepository.save(issue));
    }

    private void saveTags(Issue issue, List<Tag> tags){
/*        Set<String> tagNames = tags.stream().map(Tag::getName).collect(Collectors.toSet());
        List<Tag> existingTags = tagsRepository.findByNameIn(tagNames);


        Set<String> existingTagNames = existingTags.stream().map(Tag::getName).collect(Collectors.toSet());
        List<Tag> newTags = tags.stream()
                .filter(tag -> !existingTagNames.contains(tag.getName()))
                .collect(Collectors.toList());

        if (!newTags.isEmpty()) {
            tagsRepository.saveAll(newTags);
        }

        existingTags.addAll(newTags);*/

        issue.setTags(tags);

/*        for (Tag tag : existingTags) {
            tag.getIssues().add(issue);
        }*/
    }


    @Transactional(readOnly = true)
    public List<IssueResponseDTO> findAll() {
        return issuesMapper.toIssueResponseList(issuesRepository.findAll());
    }

    @Transactional(readOnly = true)
    public IssueResponseDTO findById(long id) {
        return issuesMapper.toIssueResponse(
                issuesRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("Issue with such id does not exist")));
    }

    @Transactional
    public void deleteById(long id) {
        if (!issuesRepository.existsById(id)) {
            throw new NotFoundException("Issue not found");
        }
        issuesRepository.deleteById(id);
    }

    @Transactional
    public IssueResponseDTO update(IssueRequestDTO issueRequestDTO) {
        Issue issue = issuesMapper.toIssue(issueRequestDTO);
        Issue oldIssue = issuesRepository.findById(issue.getId()).orElseThrow(() -> new NotFoundException("Old issue not found"));
        Long userId = issueRequestDTO.getUserId();

        if (userId != null) {
            setUser(issue, userId);
        }
        issue.setCreated(oldIssue.getCreated());
        issue.setModified(new Date());
        return issuesMapper.toIssueResponse(issuesRepository.save(issue));
    }

    public boolean existsByTitle(String title){
        return issuesRepository.existsByTitle(title);
    }

    public boolean existsById(Long id){
        return issuesRepository.existsById(id);
    }
}
