package io.javabrains.springbootstarter.topic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TopicService {

	@Autowired
	private TopicRepository topicRepository;

	private List<Topic> topics = new ArrayList<Topic>(Arrays.asList(

			new Topic("Spring1", "Boot", "Framework"), new Topic("Spring2", "Boot", "Framework"),
			new Topic("Spring3", "Boot", "Framework"), new Topic("Spring4", "Boot", "Framework")));

	public List<Topic> getAllTopics() {
		List<Topic> topics = new ArrayList<>();
		topicRepository.findAll().forEach(topics::add);
		return topics;
	}

	public Topic getTopic(String id) {
		return topics.stream().filter(t -> t.getId().equals(id)).findFirst().get();
	}

	public void addTopic(Topic topic) {

		topicRepository.save(topic);
	}

	public void updateTopic(Topic topic) {

		topicRepository.save(topic);
	}

	public void deleteTopic(String id) {

		topicRepository.delete(id);
	}

}
